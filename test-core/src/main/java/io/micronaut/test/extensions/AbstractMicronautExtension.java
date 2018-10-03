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

package io.micronaut.test.extensions;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.annotation.Property;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.runtime.context.scope.refresh.RefreshEvent;
import io.micronaut.runtime.context.scope.refresh.RefreshScope;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.condition.TestActiveCondition;

import javax.annotation.Nullable;
import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Abstract base class for both JUnit 5 and Spock.
 *
 * @author graemerocher
 * @since 1.0
 * @param <C> The extension context
 */
public abstract class AbstractMicronautExtension<C> {
    protected ApplicationContext applicationContext;
    protected EmbeddedApplication embeddedApplication;
    protected RefreshScope refreshScope;
    protected BeanDefinition<?> specDefinition;
    protected Map<String, Object> testProperties = new LinkedHashMap<>();
    protected Map<String, Object> oldValues = new LinkedHashMap<>();

    /**
     * Executed before tests within a class are run.
     *
     * @param context The test context
     * @param testClass The test class
     * @param testAnnotation The test annotation
     */
    protected void beforeClass(C context, Class<?> testClass, @Nullable MicronautTest testAnnotation) {
        if (testAnnotation != null) {

            final ApplicationContextBuilder builder = ApplicationContext.build();
            final Package aPackage = testClass.getPackage();
            builder.packages(aPackage.getName());

            final Property[] ps = testClass.getAnnotationsByType(Property.class);
            if (ps != null) {
                for (Property property : ps) {
                    testProperties.put(property.name(), property.value());
                }
            }
            testProperties.put(TestActiveCondition.ACTIVE_SPEC_NAME, aPackage.getName() + "." + testClass.getSimpleName());
            final Class<?> application = testAnnotation.application();
            if (application != void.class) {
                builder.mainClass(application);
            }
            builder.packages(testAnnotation.packages())
                   .environments(testAnnotation.environments());

            builder.propertySources(io.micronaut.context.env.PropertySource.of(testProperties));
            this.applicationContext = builder.build();
            startApplicationContext();
            specDefinition = applicationContext.findBeanDefinition(testClass).orElse(null);
            if (applicationContext.containsBean(EmbeddedApplication.class)) {
                embeddedApplication = applicationContext.getBean(EmbeddedApplication.class);
                embeddedApplication.start();
            }
            refreshScope = applicationContext.findBean(RefreshScope.class).orElse(null);
        }
    }

    protected void beforeEach(C context, @Nullable Object testInstance, @Nullable AnnotatedElement method) {
        if (method != null) {
            final Property[] ps = method.getAnnotationsByType(Property.class);
            if (ps != null) {
                for (Property property : ps) {
                    final String name = property.name();
                    oldValues.put(name,
                            testProperties.put(name, property.value())
                    );
                }
            }

            if (!oldValues.isEmpty()) {
                final Map<String, Object> diff = applicationContext.getEnvironment().refreshAndDiff();
                refreshScope.onApplicationEvent(new RefreshEvent(diff));
            }
        }

        if (testInstance != null) {
            if (applicationContext != null) {
                if (refreshScope != null) {
                    refreshScope.onApplicationEvent(new RefreshEvent(Collections.singletonMap(
                            TestActiveCondition.ACTIVE_MOCKS, "changed"
                    )));
                }
                applicationContext.inject(testInstance);
                alignMocks(context, testInstance);
            }
        }
    }

    /**
     * Executed after the class is complete.
     *
     * @param context the context
     */
    protected void afterClass(C context) {
        if (embeddedApplication != null) {
            embeddedApplication.stop();
        } else if (applicationContext != null) {
            applicationContext.stop();
        }
        embeddedApplication = null;
        applicationContext = null;
    }


    /**
     * Executed after each test completes.
     *
     * @param context The context
     */
    public void afterEach(C context) {
        if (refreshScope != null) {
            if (!oldValues.isEmpty()) {
                testProperties.putAll(oldValues);
                final Map<String, Object> diff = applicationContext.getEnvironment().refreshAndDiff();
                refreshScope.onApplicationEvent(new RefreshEvent(diff));
            }
        }
        oldValues.clear();
    }

    protected void startApplicationContext() {
        applicationContext.start();
    }

    protected abstract void alignMocks(C context, Object instance);
}
