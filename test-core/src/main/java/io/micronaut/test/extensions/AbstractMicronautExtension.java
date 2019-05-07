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
import io.micronaut.context.env.PropertySource;
import io.micronaut.context.env.PropertySourceLoader;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.service.ServiceDefinition;
import io.micronaut.core.io.service.SoftServiceLoader;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.runtime.context.scope.refresh.RefreshEvent;
import io.micronaut.runtime.context.scope.refresh.RefreshScope;
import io.micronaut.test.annotation.AnnotationUtils;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.condition.TestActiveCondition;
import io.micronaut.test.transaction.TestTransactionInterceptor;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AnnotatedElement;
import java.util.*;

/**
 * Abstract base class for both JUnit 5 and Spock.
 *
 * @author graemerocher
 * @since 1.0
 * @param <C> The extension context
 */
public abstract class AbstractMicronautExtension<C> implements TestTransactionInterceptor  {
    public static final String DISABLED_MESSAGE = "Test is not bean. Either the test does not satisfy requirements defined by @Requires or annotation processing is not enabled. If the latter ensure annotation processing is enabled in your IDE.";
    public static final String MISCONFIGURED_MESSAGE = "@MicronautTest used on test but no bean definition for the test present. This error indicates a misconfigured build or IDE. Please add the 'micronaut-inject-java' annotation processor to your test processor path (for Java this is the testAnnotationProcessor scope, for Kotlin kaptTest and for Groovy testCompile). See the documentation for reference: https://micronaut-projects.github.io/micronaut-test/latest/guide/";
    private static Map<String, PropertySourceLoader> loaderMap;
    protected ApplicationContext applicationContext;
    protected EmbeddedApplication embeddedApplication;
    protected RefreshScope refreshScope;
    protected BeanDefinition<?> specDefinition;
    protected Map<String, Object> testProperties = new LinkedHashMap<>();
    protected Map<String, Object> oldValues = new LinkedHashMap<>();
    private boolean rollback = true;
    private boolean transactional = true;

    @Override
    public void begin() {
        if (transactional && applicationContext != null) {
            final TestTransactionInterceptor testTransactionInterceptor = applicationContext.getBean(TestTransactionInterceptor.class);
            testTransactionInterceptor.begin();
        }
    }

    @Override
    public void commit() {
        if (transactional && applicationContext != null && !rollback) {
            final TestTransactionInterceptor testTransactionInterceptor = applicationContext.getBean(TestTransactionInterceptor.class);
            testTransactionInterceptor.commit();
        }

    }

    @Override
    public void rollback() {
        if (transactional && applicationContext != null && rollback) {
            final TestTransactionInterceptor testTransactionInterceptor = applicationContext.getBean(TestTransactionInterceptor.class);
            testTransactionInterceptor.rollback();
        }

    }

    /**
     * Executed before tests within a class are run.
     *
     * @param context The test context
     * @param testClass The test class
     * @param testAnnotation The test annotation
     */
    protected void beforeClass(C context, Class<?> testClass, @Nullable MicronautTest testAnnotation) {
        if (testAnnotation != null) {
            this.rollback = testAnnotation.rollback();
            this.transactional = testAnnotation.transactional();

            final ApplicationContextBuilder builder = ApplicationContext.build();
            final Package aPackage = testClass.getPackage();
            builder.packages(aPackage.getName());

            final List<Property> ps = AnnotationUtils.findRepeatableAnnotations(testClass, Property.class);
            for (Property property : ps) {
                testProperties.put(property.name(), property.value());
            }

            String[] propertySources = testAnnotation.propertySources();
            if (ArrayUtils.isNotEmpty(propertySources)) {

                Map<String, PropertySourceLoader> loaderMap = readPropertySourceLoaderMap();
                ResourceResolver resourceResolver = new ResourceResolver();

                for (String propertySource : propertySources) {
                    String ext = NameUtils.extension(propertySource);
                    if (StringUtils.isNotEmpty(ext)) {

                        String filename = NameUtils.filename(propertySource);
                        PropertySourceLoader loader = loaderMap.get(ext);

                        if (loader != null) {
                            Optional<InputStream> resourceAsStream = resourceResolver.getResourceAsStream(propertySource);
                            InputStream inputStream = resourceAsStream.orElse(testClass.getResourceAsStream(propertySource));

                            if (inputStream != null) {
                                Map<String, Object> properties;
                                try {
                                    properties = loader.read(filename, inputStream);
                                    builder.propertySources(PropertySource.of(filename, properties));
                                } catch (IOException e) {
                                    throw new RuntimeException("Error loading property source reference for @MicronautTest: " + filename);
                                } finally {
                                    try {
                                        inputStream.close();
                                    } catch (IOException e) {
                                        // ignore
                                    }
                                }
                            }
                        }
                    }
                }
            }

            testProperties.put(TestActiveCondition.ACTIVE_SPEC_NAME, aPackage.getName() + "." + testClass.getSimpleName());
            final Class<?> application = testAnnotation.application();
            if (application != void.class) {
                builder.mainClass(application);
            }
            String[] environments = testAnnotation.environments();
            if (environments.length == 0) {
                environments = new String[]{"test"};
            }
            builder.packages(testAnnotation.packages())
                   .environments(environments);

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
                refreshScope.onRefreshEvent(new RefreshEvent(diff));
            }
        }

        if (testInstance != null) {
            if (applicationContext != null) {
                if (refreshScope != null) {
                    refreshScope.onRefreshEvent(new RefreshEvent(Collections.singletonMap(
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
                refreshScope.onRefreshEvent(new RefreshEvent(diff));
            }
        }
        oldValues.clear();
    }

    protected void startApplicationContext() {
        applicationContext.start();
    }

    protected boolean isTestSuiteBeanPresent(Class<?> requiredTestClass) {
        return ClassUtils.isPresent(requiredTestClass.getPackage().getName() + ".$" + requiredTestClass.getSimpleName() + "Definition", requiredTestClass.getClassLoader());
    }

    protected abstract void alignMocks(C context, Object instance);

    private Map<String, PropertySourceLoader> readPropertySourceLoaderMap() {
        Map<String, PropertySourceLoader> loaderMap = AbstractMicronautExtension.loaderMap;
        if (loaderMap == null) {
            loaderMap = new HashMap<>();
            AbstractMicronautExtension.loaderMap = loaderMap;
            SoftServiceLoader<PropertySourceLoader> loaders = SoftServiceLoader.load(PropertySourceLoader.class);
            for (ServiceDefinition<PropertySourceLoader> loader : loaders) {
                if (loader.isPresent()) {
                    PropertySourceLoader psl = loader.load();
                    Set<String> extensions = psl.getExtensions();
                    for (String extension : extensions) {
                        loaderMap.put(extension, psl);
                    }
                }
            }
        }
        return loaderMap;
    }
}
