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
import io.micronaut.core.reflect.InstantiationUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.runtime.context.scope.refresh.RefreshEvent;
import io.micronaut.runtime.context.scope.refresh.RefreshScope;
import io.micronaut.test.annotation.AnnotationUtils;
import io.micronaut.test.annotation.MicronautTestValue;
import io.micronaut.test.condition.TestActiveCondition;
import io.micronaut.test.context.TestContext;
import io.micronaut.test.context.TestExecutionListener;
import io.micronaut.test.support.TestPropertyProvider;

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
public abstract class AbstractMicronautExtension<C> implements TestExecutionListener {
    public static final String TEST_ROLLBACK = "micronaut.test.rollback";
    public static final String TEST_TRANSACTIONAL = "micronaut.test.transactional";
    public static final String TEST_TRANSACTION_MODE = "micronaut.test.transaction-mode";
    public static final String DISABLED_MESSAGE = "Test is not bean. Either the test does not satisfy requirements defined by @Requires or annotation processing is not enabled. If the latter ensure annotation processing is enabled in your IDE.";
    public static final String MISCONFIGURED_MESSAGE = "@MicronautTest used on test but no bean definition for the test present. This error indicates a misconfigured build or IDE. Please add the 'micronaut-inject-java' annotation processor to your test processor path (for Java this is the testAnnotationProcessor scope, for Kotlin kaptTest and for Groovy testCompile). See the documentation for reference: https://micronaut-projects.github.io/micronaut-test/latest/guide/";
    private static Map<String, PropertySourceLoader> loaderMap;
    protected ApplicationContext applicationContext;
    protected EmbeddedApplication embeddedApplication;
    protected RefreshScope refreshScope;
    protected BeanDefinition<?> specDefinition;
    protected Map<String, Object> testProperties = new LinkedHashMap<>();
    protected Map<String, Object> oldValues = new LinkedHashMap<>();

    private MicronautTestValue testAnnotationValue;
    private ApplicationContextBuilder builder = ApplicationContext.build();

    /** {@inheritDoc} */
    @Override
    public void beforeTestExecution(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::beforeTestExecution, testContext);
    }

    @Override
    public void beforeCleanupTest(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::beforeCleanupTest, testContext);
    }

    @Override
    public void afterCleanupTest(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::afterCleanupTest, testContext);
    }

    /** {@inheritDoc} */
    @Override
    public void afterTestExecution(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::afterTestExecution, testContext);
    }

    /** {@inheritDoc} */
    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::beforeTestClass, testContext);
    }

    /** {@inheritDoc} */
    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::afterTestClass, testContext);
    }

    @Override
    public void beforeSetupTest(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::beforeSetupTest, testContext);
    }

    @Override
    public void afterSetupTest(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::afterSetupTest, testContext);
    }

    /** {@inheritDoc} */
    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::beforeTestMethod, testContext);
    }

    /** {@inheritDoc} */
    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::afterTestMethod, testContext);
    }

    /**
     * Actually fires the execution listener.
     *
     * @param callback the execution listener callback
     * @param testContext the test context
     * @throws Exception allows any exception to propagate
     */
    private void fireListeners(TestListenerCallback callback, TestContext testContext) throws Exception {
        if (applicationContext != null) {

            Collection<TestExecutionListener> listeners = applicationContext.getBeansOfType(TestExecutionListener.class);
            for (TestExecutionListener listener : listeners) {
                callback.apply(listener, testContext);
            }

        }
    }

    /**
     * Executed before tests within a class are run.
     *
     * @param context The test context
     * @param testClass The test class
     * @param testAnnotationValue The test annotation values
     */
    protected void beforeClass(C context, Class<?> testClass, @Nullable MicronautTestValue testAnnotationValue) {
        if (testAnnotationValue != null) {
            Class<? extends ApplicationContextBuilder>[] cb = testAnnotationValue.contextBuilder();
            if (ArrayUtils.isNotEmpty(cb)) {
                this.builder = InstantiationUtils.instantiate(cb[0]);
            }
            this.testAnnotationValue = testAnnotationValue;

            final Package aPackage = testClass.getPackage();
            builder.packages(aPackage.getName());

            final List<Property> ps = AnnotationUtils.findRepeatableAnnotations(testClass, Property.class);
            for (Property property : ps) {
                testProperties.put(property.name(), property.value());
            }

            String[] propertySources = testAnnotationValue.propertySources();
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
            if (TestPropertyProvider.class.isAssignableFrom(testClass)) {
                resolveTestProperties(context, testAnnotationValue, testProperties);
            }
            testProperties.put(TestActiveCondition.ACTIVE_SPEC_NAME, aPackage.getName() + "." + testClass.getSimpleName());
            testProperties.put(TestActiveCondition.ACTIVE_SPEC_CLAZZ, testClass);
            testProperties.put(TEST_ROLLBACK, String.valueOf(testAnnotationValue.rollback()));
            testProperties.put(TEST_TRANSACTIONAL, String.valueOf(testAnnotationValue.transactional()));
            testProperties.put(TEST_TRANSACTION_MODE, String.valueOf(testAnnotationValue.transactionMode()));
            final Class<?> application = testAnnotationValue.application();
            if (application != void.class) {
                builder.mainClass(application);
            }
            String[] environments = testAnnotationValue.environments();
            if (environments.length == 0) {
                environments = new String[]{"test"};
            }
            builder.packages(testAnnotationValue.packages())
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

    /**
     * Resolves any test properties.
     *  @param context The test context
     * @param testAnnotationValue The test annotation
     * @param testProperties The test properties
     */
    protected abstract void resolveTestProperties(C context, MicronautTestValue testAnnotationValue, Map<String, Object> testProperties);

    /**
     * To be called by the different implementations before each test method.
     *
     * @param context The test context
     * @param testInstance The test instance
     * @param method The test method
     * @param propertyAnnotations The {@code @Property} annotations found in the test method, if any
     */
    protected void beforeEach(C context, @Nullable Object testInstance, @Nullable AnnotatedElement method, List<Property> propertyAnnotations) {
        int testCount = (int) testProperties.compute("micronaut.test.count", (k, oldCount) -> (int) (oldCount != null ? oldCount : 0) + 1);
        if (method != null) {
            if (propertyAnnotations != null && !propertyAnnotations.isEmpty()) {
                for (Property property : propertyAnnotations) {
                    final String name = property.name();
                    oldValues.put(name,
                            testProperties.put(name, property.value())
                    );
                }
            } else {
                oldValues.forEach((k, v) -> testProperties.put(k, v));
            }

            if (testAnnotationValue.rebuildContext() && testCount > 1) {
                embeddedApplication.stop();
                applicationContext.stop();
                applicationContext = builder.build();
                startApplicationContext();
                embeddedApplication.start();
            } else if (!oldValues.isEmpty()) {
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
     * @throws Exception allows any exception to propagate
     */
    public void afterEach(C context) throws Exception {
        if (refreshScope != null) {
            if (!oldValues.isEmpty()) {
                testProperties.putAll(oldValues);
                final Map<String, Object> diff = applicationContext.getEnvironment().refreshAndDiff();
                refreshScope.onRefreshEvent(new RefreshEvent(diff));
            }
        }
        oldValues.clear();
    }

    /**
     * Starts the application context.
     */
    protected void startApplicationContext() {
        applicationContext.start();
    }

    /**
     * @param requiredTestClass The test class
     * @return true if the te given class has a bean definition class in the classpath (ie: the annotation processor has been run correctly)
     */
    protected boolean isTestSuiteBeanPresent(Class<?> requiredTestClass) {
        return ClassUtils.isPresent(requiredTestClass.getPackage().getName() + ".$" + requiredTestClass.getSimpleName() + "Definition", requiredTestClass.getClassLoader());
    }

    /**
     * @param context The context
     * @param instance The mock instance to inject
     */
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

    /**
     * Fires events to the {@link TestExecutionListener}s.
     */
    @FunctionalInterface
    private interface TestListenerCallback {

        void apply(TestExecutionListener listener, TestContext context) throws Exception;

    }

}
