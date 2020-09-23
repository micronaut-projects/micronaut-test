/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.test.extensions.spock;

import io.micronaut.aop.InterceptedProxy;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.MethodInjectionPoint;
import io.micronaut.test.annotation.AnnotationUtils;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MicronautTestValue;
import io.micronaut.test.context.TestContext;
import io.micronaut.test.extensions.AbstractMicronautExtension;
import io.micronaut.test.support.TestPropertyProvider;
import org.spockframework.mock.MockUtil;
import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.SpecInfo;
import spock.lang.Specification;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Extension for Spock.
 *
 * @param <T> The MicronautTest annotation
 * @author graemerocher
 * @since 1.0
 */
public class MicronautSpockExtension<T extends Annotation> extends AbstractMicronautExtension<IMethodInvocation> implements IAnnotationDrivenExtension<T> {

    private Queue<Object> creatableMocks = new ConcurrentLinkedDeque<>();
    private Queue<Object> singletonMocks = new ConcurrentLinkedDeque<>();
    private MockUtil mockUtil = new MockUtil();

    @Override
    public void visitSpecAnnotation(T annotation, SpecInfo spec) {

        spec.getAllFeatures().forEach(feature -> {

            feature.addInterceptor(invocation -> {
                try {
                    beforeTestMethod(buildContext(invocation, null));
                    invocation.proceed();
                } finally {
                    afterTestMethod(buildContext(invocation, null));
                }
            });

            feature.getFeatureMethod().addInterceptor(invocation -> {
                try {
                    beforeTestExecution(buildContext(invocation, null));
                    invocation.proceed();
                    afterTestExecution(buildContext(invocation, null));
                } catch (Exception e) {
                    afterTestExecution(buildContext(invocation, e));
                    throw e;
                }
            });
        });

        spec.addSetupSpecInterceptor(invocation -> {
                    MicronautTestValue micronautTestValue;
                    MicronautTest micronautTest = spec.getAnnotation(MicronautTest.class);
                    if (micronautTest == null) {
                        micronautTestValue = AnnotationUtils.buildValueObject(spec.getAnnotation(io.micronaut.test.annotation.MicronautTest.class));
                    } else {
                        micronautTestValue = buildValueObject(micronautTest);
                    }
                    beforeClass(invocation, spec.getReflection(), micronautTestValue);
                    if (specDefinition == null) {
                        if (!isTestSuiteBeanPresent(spec.getReflection())) {
                            throw new InvalidSpecException(MISCONFIGURED_MESSAGE);
                        } else {
                            final List<FeatureInfo> features = invocation.getSpec().getFeatures();
                            for (FeatureInfo feature : features) {
                                feature.setSkipped(true);
                            }
                        }
                    } else {
                        List<FieldInfo> fields = spec.getAllFields();
                        for (FieldInfo field : fields) {
                            if (field.isShared() && field.getAnnotation(Inject.class) != null) {
                                applicationContext.inject(invocation.getSharedInstance());
                                break;
                            }
                        }
                    }
                    beforeTestClass(buildContext(invocation, null));
                    invocation.proceed();
            }
        );

        spec.addCleanupSpecInterceptor(invocation -> {
            afterTestClass(buildContext(invocation, null));
            afterClass(invocation);

            invocation.proceed();
            singletonMocks.clear();
        });

        spec.addSetupInterceptor(invocation -> {
            final Object instance = invocation.getInstance();
            final Method method = invocation.getFeature().getFeatureMethod().getReflection();
            List<Property> propertyAnnotations = Arrays.asList(method.getAnnotationsByType(Property.class));
            beforeEach(invocation, instance, method, propertyAnnotations);
            for (Object mock : creatableMocks) {
                mockUtil.attachMock(mock, (Specification) instance);
            }
            for (Object mock : singletonMocks) {
                mockUtil.attachMock(mock, (Specification) instance);
            }
            try {
                beforeSetupTest(buildContext(invocation, null));
                invocation.proceed();
            } finally {
                afterSetupTest(buildContext(invocation, null));
            }
        });

        spec.addCleanupInterceptor(invocation -> {
            for (Object mock : creatableMocks) {
                mockUtil.detachMock(mock);
            }
            for (Object mock : singletonMocks) {
                mockUtil.detachMock(mock);
            }
            creatableMocks.clear();
            afterEach(invocation);
            try {
                beforeCleanupTest(buildContext(invocation, null));
                invocation.proceed();
            } finally {
                afterCleanupTest(buildContext(invocation, null));
            }
        });
    }

    private MicronautTestValue buildValueObject(MicronautTest micronautTest) {
        if (micronautTest != null) {
            return new MicronautTestValue(
                    micronautTest.application(),
                    micronautTest.environments(),
                    micronautTest.packages(),
                    micronautTest.propertySources(),
                    micronautTest.rollback(),
                    micronautTest.transactional(),
                    micronautTest.rebuildContext(),
                    micronautTest.contextBuilder(),
                    micronautTest.transactionMode());
        } else {
            return null;
        }
    }

    private TestContext buildContext(IMethodInvocation invocation, Throwable exception) {
        return new TestContext(
            applicationContext,
            Optional.ofNullable(invocation.getSpec()).map(SpecInfo::getReflection).orElse(null),
            Optional.ofNullable(invocation.getFeature()).map(FeatureInfo::getFeatureMethod).map(MethodInfo::getReflection).orElse(null),
            invocation.getInstance(),
            exception);
    }

    @Override
    public void visitFeatureAnnotation(T annotation, FeatureInfo feature) {
        throw new InvalidSpecException("@%s may not be applied to feature methods")
            .withArgs(annotation.annotationType().getSimpleName());
    }

    @Override
    public void visitFixtureAnnotation(T annotation, MethodInfo fixtureMethod) {
        throw new InvalidSpecException("@%s may not be applied to fixture methods")
            .withArgs(annotation.annotationType().getSimpleName());
    }

    @Override
    public void visitFieldAnnotation(T annotation, FieldInfo field) {
        throw new InvalidSpecException("@%s may not be applied to fields")
            .withArgs(annotation.annotationType().getSimpleName());
    }

    @Override
    public void visitSpec(SpecInfo spec) {
        // no-op
    }

    @Override
    protected void resolveTestProperties(IMethodInvocation context, MicronautTestValue testAnnotationValue, Map<String, Object> testProperties) {
        Object sharedInstance = context.getSharedInstance();
        if (sharedInstance instanceof TestPropertyProvider) {
            Map<String, String> properties = ((TestPropertyProvider) sharedInstance).getProperties();
            if (CollectionUtils.isNotEmpty(properties)) {
                testProperties.putAll(properties);
            }
        }
    }

    @Override
    protected void startApplicationContext() {
        applicationContext.registerSingleton((BeanCreatedEventListener) event -> {
            final Object bean = event.getBean();
            if (mockUtil.isMock(bean)) {
                if (event.getBeanDefinition().isSingleton()) {
                    singletonMocks.add(bean);
                } else {
                    creatableMocks.add(bean);
                }
            }
            return bean;
        });

        super.startApplicationContext();
    }

    @Override
    protected void alignMocks(IMethodInvocation context, Object instance) {
        for (MethodInjectionPoint injectedMethod : specDefinition.getInjectedMethods()) {
            final Argument<?>[] args = injectedMethod.getArguments();
            if (args.length == 1) {
                final Optional<FieldInfo> fld = context.getSpec().getAllFields().stream().filter(f -> f.getName().equals(args[0].getName())).findFirst();
                if (fld.isPresent()) {
                    final FieldInfo fieldInfo = fld.get();
                    final Object fieldInstance = fieldInfo.readValue(
                        instance
                    );
                    if (fieldInstance instanceof InterceptedProxy) {
                        Object interceptedTarget = ((InterceptedProxy) fieldInstance).interceptedTarget();
                        if (mockUtil.isMock(interceptedTarget)) {
                            fieldInfo.writeValue(instance, interceptedTarget);
                        }
                    }
                }
            }
        }
    }
}
