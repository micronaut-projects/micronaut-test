/*
 * Copyright 2017-2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micronaut.test.spock;

import io.micronaut.aop.InterceptedProxy;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.PropertySource;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.MethodInjectionPoint;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.runtime.context.scope.refresh.RefreshEvent;
import io.micronaut.runtime.context.scope.refresh.RefreshScope;
import io.micronaut.test.spock.annotation.MicronautTest;
import io.micronaut.test.spock.annotation.MockBean;
import io.micronaut.test.spock.annotation.SpecActiveCondition;
import org.spockframework.mock.MockUtil;
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.SpecInfo;
import spock.lang.Specification;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;

public class RunApplicationExtension extends AbstractAnnotationDrivenExtension<MicronautTest> {

    private ApplicationContext applicationContext;
    private EmbeddedApplication embeddedApplication;
    private RefreshScope refreshScope;
    private Queue<Object> createdMocks = new ConcurrentLinkedDeque<>();
    private MockUtil mockUtil = new MockUtil();
    private BeanDefinition<?> specDefinition;
    private Map<String, Object> specProperties = new LinkedHashMap<>();

    @Override
    public void visitFieldAnnotation(MicronautTest annotation, FieldInfo field) {
        super.visitFieldAnnotation(annotation, field);
    }

    @Override
    public void visitSpecAnnotation(MicronautTest annotation, SpecInfo spec) {

        final ApplicationContextBuilder builder = ApplicationContext.build();
        final Property p = spec.getAnnotation(Property.class);


        if (p != null) {
            specProperties.put(p.name(), p.value());
        }
        final PropertySource ps = spec.getAnnotation(PropertySource.class);
        if (ps != null) {
            for (Property property : ps.value()) {
                specProperties.put(property.name(), property.value());
            }
        }
        specProperties.put(SpecActiveCondition.ACTIVE_SPEC_NAME, spec.getPackage() + "." + spec.getName());
        final Class<?> application = annotation.application();
        if (application != void.class) {
            builder.mainClass(application);
        }

        builder.propertySources(io.micronaut.context.env.PropertySource.of(specProperties));
        this.applicationContext = builder.build();


        spec.addSetupSpecInterceptor(new SetupInterceptor());

        spec.addCleanupSpecInterceptor(invocation -> {
            if (embeddedApplication != null) {
                embeddedApplication.stop();
            } else if(applicationContext != null) {
                applicationContext.stop();
            }
        });

        Map<String, Object> oldValues = new LinkedHashMap<>();
        spec.addSetupInterceptor(invocation -> {
            final MethodInfo methodInfo = invocation.getFeature().getFeatureMethod();

            final Property featureProp = methodInfo.getAnnotation(Property.class);

            if (featureProp != null) {
                final String n = featureProp.name();
                final Object previous = specProperties.put(n, featureProp.value());
                oldValues.put(n, previous);
            }
            final PropertySource featureSource = methodInfo.getAnnotation(PropertySource.class);
            if (featureSource != null) {
                for (Property property : featureSource.value()) {
                    final String n = property.name();
                    final Object previous = specProperties.put(n, property.value());
                    oldValues.put(n, previous);
                }
            }
            if (!oldValues.isEmpty()) {
                final Map<String, Object> diff = applicationContext.getEnvironment().refreshAndDiff();
                refreshScope.onApplicationEvent(new RefreshEvent(diff));
            }

            final Object instance = invocation.getInstance();
            if (applicationContext != null) {
                if (refreshScope != null) {
                    refreshScope.onApplicationEvent(new RefreshEvent(Collections.singletonMap(
                            SpecActiveCondition.ACTIVE_MOCKS, "changed"
                    )));
                }
                applicationContext.inject(instance);
                alignMocks(invocation, instance);
            }
            for (Object createdMock : createdMocks) {
                mockUtil.attachMock(createdMock, (Specification) instance);
            }
        });

        spec.addCleanupInterceptor(invocation -> {
            for (Object createdMock : createdMocks) {
                mockUtil.detachMock(createdMock);
            }
            createdMocks.clear();
            if (!oldValues.isEmpty()) {
                specProperties.putAll(oldValues);
                final Map<String, Object> diff = applicationContext.getEnvironment().refreshAndDiff();
                refreshScope.onApplicationEvent(new RefreshEvent(diff));
            }
            oldValues.clear();
        });
    }

    private void alignMocks(IMethodInvocation invocation, Object instance) {
        for (MethodInjectionPoint injectedMethod : specDefinition.getInjectedMethods()) {
            final Argument<?>[] args = injectedMethod.getArguments();
            if (args.length == 1) {
                final Optional<FieldInfo> fld = invocation.getSpec().getFields().stream().filter(f -> f.getName().equals(args[0].getName())).findFirst();
                if (fld.isPresent()) {
                    final FieldInfo fieldInfo = fld.get();
                    if (applicationContext.resolveMetadata(fieldInfo.getType()).isAnnotationPresent(MockBean.class)) {
                        final Object proxy = fieldInfo.readValue(
                                instance
                        );
                        if (proxy instanceof InterceptedProxy) {
                            final InterceptedProxy interceptedProxy = (InterceptedProxy) proxy;
                            final Object target = interceptedProxy.interceptedTarget();
                            fieldInfo.writeValue(instance, target);
                        }

                    }
                }
            }
        }
    }


    private class SetupInterceptor implements IMethodInterceptor, ApplicationContextProvider {
        @Override
        public void intercept(IMethodInvocation invocation) throws Throwable {
            final Object sharedInstance = invocation.getSharedInstance();
            applicationContext.registerSingleton((BeanCreatedEventListener) event -> {
                final Object bean = event.getBean();
                if (mockUtil.isMock(bean)) {
                    createdMocks.add(bean);
                }
                return bean;
            });
            applicationContext.start();
            if (!applicationContext.containsBean(sharedInstance.getClass())) {
                final List<FeatureInfo> features = invocation.getSpec().getFeatures();
                for (FeatureInfo feature : features) {
                    feature.setSkipped(true);
                }
            } else {
                specDefinition = applicationContext.getBeanDefinition(sharedInstance.getClass());
                if (applicationContext.containsBean(EmbeddedApplication.class)) {
                    embeddedApplication = applicationContext.getBean(EmbeddedApplication.class);
                    embeddedApplication.start();
                }

                applicationContext.inject(sharedInstance);
                refreshScope = applicationContext.findBean(RefreshScope.class).orElse(null);
            }
            invocation.proceed();
        }

        @Override
        public ApplicationContext getApplicationContext() {
            return applicationContext;
        }
    }
}
