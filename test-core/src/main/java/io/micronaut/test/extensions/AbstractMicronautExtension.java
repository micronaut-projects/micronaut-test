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

import io.micronaut.aop.Interceptor;
import io.micronaut.aop.InterceptorRegistry;
import io.micronaut.aop.chain.InterceptorChain;
import io.micronaut.aop.chain.MethodInterceptorChain;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.context.env.PropertySourceLoader;
import io.micronaut.context.env.PropertySourcesLocator;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.io.scan.ClassClassPathResourceLoader;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import io.micronaut.core.io.scan.CombinedClassPathResourceLoader;
import io.micronaut.core.io.service.SoftServiceLoader;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.InstantiationUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.ProxyBeanDefinition;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.runtime.context.scope.refresh.RefreshEvent;
import io.micronaut.runtime.context.scope.refresh.RefreshScope;
import io.micronaut.test.annotation.AnnotationUtils;
import io.micronaut.test.annotation.MicronautTestValue;
import io.micronaut.test.annotation.Sql;
import io.micronaut.test.condition.TestActiveCondition;
import io.micronaut.test.context.TestContext;
import io.micronaut.test.context.TestExecutionListener;
import io.micronaut.test.context.TestMethodInterceptor;
import io.micronaut.test.context.TestMethodInvocationContext;
import io.micronaut.test.support.TestPropertyProvider;
import io.micronaut.test.support.TestPropertyProviderFactory;
import io.micronaut.test.support.sql.TestSqlAnnotationHandler;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract base class for both JUnit 5 and Spock.
 *
 * @param <C> The extension context
 * @author graemerocher
 * @since 1.0
 */
public abstract class AbstractMicronautExtension<C> implements TestExecutionListener, TestMethodInterceptor<Object> {
    public static final String TEST_ROLLBACK = "micronaut.test.rollback";
    public static final String TEST_TRANSACTIONAL = "micronaut.test.transactional";
    public static final String TEST_TRANSACTION_MODE = "micronaut.test.transaction-mode";
    public static final String DISABLED_MESSAGE = "Test is not bean. Either the test does not satisfy requirements defined by @Requires or annotation processing is not enabled. If the latter ensure annotation processing is enabled in your IDE.";
    public static final String MISCONFIGURED_MESSAGE = """
        @MicronautTest used on test but no bean definition for the test present. \
        This error indicates a misconfigured build or IDE. \
        Please check 'micronaut.processing.group' or 'micronaut.processing.annotations' in your build. \
        Add the 'micronaut-inject-java' annotation processor to your test processor path (for Java this is the testAnnotationProcessor scope, for Kotlin kaptTest and for Groovy testCompile). \
        See the documentation for reference: https://micronaut-projects.github.io/micronaut-test/latest/guide/""";
    /**
     * The name of the property source that contains test properties.
     */
    public static final String TEST_PROPERTY_SOURCE = "test-properties";
    protected ApplicationContext applicationContext;
    protected EmbeddedApplication<?> embeddedApplication;
    protected RefreshScope refreshScope;
    protected BeanDefinition<?> specDefinition;
    protected Map<String, Object> testProperties = new LinkedHashMap<>();
    protected Map<String, Object> oldValues = new LinkedHashMap<>();
    protected MicronautTestValue testAnnotationValue;
    protected Class<?> testClass;

    private ApplicationContextBuilder builder = ApplicationContext.builder();
    private List<TestExecutionListener> listeners;
    private List<TestMethodInterceptor<Object>> interceptors;

    /**
     * @return True if there are interceptors
     */
    protected boolean hasInterceptors() {
        return !interceptors.isEmpty();
    }

    @Override
    public Object interceptBeforeEach(TestMethodInvocationContext<Object> methodInvocationContext) throws Throwable {
        return interceptBeforeEach(methodInvocationContext, interceptors);
    }

    @Override
    public Object interceptAfterEach(TestMethodInvocationContext<Object> methodInvocationContext) throws Throwable {
        return interceptAfterEach(methodInvocationContext, interceptors);
    }

    @Override
    public Object interceptTest(TestMethodInvocationContext<Object> methodInvocationContext) throws Throwable {
        return interceptEach(methodInvocationContext, interceptors);
    }

    private Object interceptBeforeEach(TestMethodInvocationContext<Object> methodInvocationContext, List<TestMethodInterceptor<Object>> interceptors) throws Throwable {
        if (interceptors == null || interceptors.isEmpty()) {
            return methodInvocationContext.proceed();
        }
        TestMethodInterceptor<Object> next = interceptors.iterator().next();
        List<TestMethodInterceptor<Object>> rest = interceptors.subList(1, interceptors.size());
        return next.interceptBeforeEach(new TestMethodInvocationContext<>() {
            @Override
            public TestContext getTestContext() {
                return methodInvocationContext.getTestContext();
            }

            @Override
            public Object proceed() throws Throwable {
                return interceptBeforeEach(methodInvocationContext, rest);
            }
        });
    }

    private Object interceptAfterEach(TestMethodInvocationContext<Object> methodInvocationContext, List<TestMethodInterceptor<Object>> interceptors) throws Throwable {
        if (interceptors == null || interceptors.isEmpty()) {
            return methodInvocationContext.proceed();
        }
        TestMethodInterceptor<Object> next = interceptors.iterator().next();
        List<TestMethodInterceptor<Object>> rest = interceptors.subList(1, interceptors.size());
        return next.interceptAfterEach(new TestMethodInvocationContext<>() {
            @Override
            public TestContext getTestContext() {
                return methodInvocationContext.getTestContext();
            }

            @Override
            public Object proceed() throws Throwable {
                return interceptAfterEach(methodInvocationContext, rest);
            }
        });
    }

    private Object interceptEach(TestMethodInvocationContext<Object> methodInvocationContext, List<TestMethodInterceptor<Object>> interceptors) throws Throwable {
        if (interceptors == null || interceptors.isEmpty()) {
            return methodInvocationContext.proceed();
        }
        TestMethodInterceptor<Object> next = interceptors.iterator().next();
        List<TestMethodInterceptor<Object>> rest = interceptors.subList(1, interceptors.size());
        return next.interceptTest(new TestMethodInvocationContext<>() {
            @Override
            public TestContext getTestContext() {
                return methodInvocationContext.getTestContext();
            }

            @Override
            public Object proceed() throws Throwable {
                return interceptEach(methodInvocationContext, rest);
            }
        });
    }

    @Override
    public void beforeTestExecution(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::beforeTestExecution, testContext, false);
    }

    @Override
    public void beforeCleanupTest(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::beforeCleanupTest, testContext, false);
    }

    @Override
    public void afterCleanupTest(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::afterCleanupTest, testContext, true);
    }

    @Override
    public void afterTestExecution(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::afterTestExecution, testContext, true);
    }

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::beforeTestClass, testContext, false);
        if (specDefinition != null && applicationContext != null) {
            TestSqlAnnotationHandler.handle(specDefinition, applicationContext, Sql.Phase.BEFORE_ALL);
        }
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::afterTestClass, testContext, true);
        if (specDefinition != null && applicationContext != null) {
            TestSqlAnnotationHandler.handle(specDefinition, applicationContext, Sql.Phase.AFTER_ALL);
        }
    }

    @Override
    public void beforeSetupTest(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::beforeSetupTest, testContext, false);
    }

    @Override
    public void afterSetupTest(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::afterSetupTest, testContext, true);
    }

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::beforeTestMethod, testContext, false);
        if (specDefinition != null && applicationContext != null) {
            TestSqlAnnotationHandler.handle(specDefinition, applicationContext, Sql.Phase.BEFORE_EACH);
        }
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::afterTestMethod, testContext, true);
        if (specDefinition != null && applicationContext != null) {
            TestSqlAnnotationHandler.handle(specDefinition, applicationContext, Sql.Phase.AFTER_EACH);
        }
    }

    /**
     * Actually fires the execution listener.
     *
     * @param callback the execution listener callback
     * @param testContext the test context
     * @throws Exception allows any exception to propagate
     */
    private void fireListeners(TestListenerCallback callback, TestContext testContext, boolean reverse) throws Exception {
        if (listeners != null) {
            if (reverse) {
                for (int i = listeners.size() - 1; i >= 0; i--) {
                    callback.apply(listeners.get(i), testContext);
                }
            } else {
                for (TestExecutionListener listener : listeners) {
                    callback.apply(listener, testContext);
                }
            }
        }
    }

    /**
     * Executed before tests within a class are run.
     *
     * @param context             The test context
     * @param testClass           The test class
     * @param testAnnotationValue The test annotation values
     */
    protected void beforeClass(C context, Class<?> testClass, @Nullable MicronautTestValue testAnnotationValue) {
        if (testAnnotationValue != null) {
            Class<? extends ApplicationContextBuilder>[] cb = testAnnotationValue.contextBuilder();
            if (ArrayUtils.isNotEmpty(cb)) {
                this.builder = InstantiationUtils.instantiate(cb[0]);
            }
            this.testAnnotationValue = testAnnotationValue;
            this.testClass = testClass;

            final Package aPackage = testClass.getPackage();
            builder.packages(aPackage.getName());
            builder.resourceResolver(CombinedClassPathResourceLoader.of(
                ClassPathResourceLoader.defaultLoader(null),
                new ClassClassPathResourceLoader(testClass)
            ));
            final List<Property> ps = AnnotationUtils.findRepeatableAnnotations(testClass, Property.class);
            for (Property property : ps) {
                testProperties.put(property.name(), property.value());
            }

            testProperties.put(TestActiveCondition.ACTIVE_SPEC_CLAZZ, testClass);
            testProperties.put(TEST_ROLLBACK, String.valueOf(testAnnotationValue.rollback()));
            testProperties.put(TEST_TRANSACTIONAL, String.valueOf(testAnnotationValue.transactional()));
            testProperties.put(TEST_TRANSACTION_MODE, String.valueOf(testAnnotationValue.transactionMode()));
            testProperties.put(Environment.DEDUCE_ENVIRONMENT_PROPERTY, String.valueOf(testAnnotationValue.deduceEnvironment()));
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
            if (TestPropertyProvider.class.isAssignableFrom(testClass)) {
                resolveTestProperties(context, testAnnotationValue, testProperties);
            }
            PropertySource testPropertySource = PropertySource.of(
                TEST_PROPERTY_SOURCE,
                testProperties
            );
            builder.propertySources(testPropertySource);

            builder.propertySourcesLocator(new PropertySourcesLocator() {
                @Override
                public Collection<PropertySource> load(Environment environment) {
                    List<PropertySource> loadedPropertySources = new ArrayList<>();
                    for (String propertySourceName : testAnnotationValue.propertySources()) {
                        String ext = NameUtils.extension(propertySourceName);
                        if (StringUtils.isEmpty(ext)) {
                            continue;
                        }
                        for (PropertySourceLoader loader : environment.getPropertySourceLoaders()) {
                            if (!loader.getExtensions().contains(ext)) {
                                continue;
                            }

                            environment.getResourceAsStream(propertySourceName).ifPresent(inputStream -> {
                                try (inputStream) {
                                    String filename = NameUtils.filename(propertySourceName);
                                    try {
                                        loadedPropertySources.add(PropertySource.of(filename, loader.read(filename, inputStream)));
                                    } catch (IOException e) {
                                        throw new RuntimeException("Error loading property source reference for @MicronautTest: " + filename);
                                    }
                                } catch (IOException e) {
                                    // ignore
                                }
                            });
                        }
                    }
                    List<TestPropertyProviderFactory> testPropertyProviderFactories = SoftServiceLoader.load(TestPropertyProviderFactory.class).collectAll();
                    if (!testPropertyProviderFactories.isEmpty()) {
                        var props = new HashMap<>(testProperties);
                        for (PropertySource source : environment.getPropertySources()) {
                            for (String key : source) {
                                props.put(key, source.get(key));
                            }
                        }
                        for (PropertySource source : loadedPropertySources) {
                            for (String key : source) {
                                props.put(key, source.get(key));
                            }
                        }
                        for (TestPropertyProviderFactory factory : testPropertyProviderFactories) {
                            var provider = factory.create(Collections.unmodifiableMap(props), testClass);
                            testProperties.putAll(provider.get());
                        }
                    }
                    if (!testProperties.isEmpty()) {
                        loadedPropertySources.add(PropertySource.of(TEST_PROPERTY_SOURCE, testProperties));
                    }
                    return loadedPropertySources;
                }
            });

            postProcessBuilder(builder);
            this.applicationContext = builder.build();
            startApplicationContext();
            specDefinition = applicationContext.findBeanDefinition(testClass).orElse(null);
            if (specDefinition instanceof ProxyBeanDefinition<?>) {
                interceptors = new ArrayList<>(interceptors);
                interceptors.add(new MicronautIntercepted(
                    specDefinition,
                    applicationContext.getBean(InterceptorRegistry.class),
                    new ArrayList<>(applicationContext.getBeanRegistrations(Argument.of(Interceptor.class), null))
                ));
            }
            if (testAnnotationValue.startApplication() && applicationContext.containsBean(EmbeddedApplication.class)) {
                embeddedApplication = applicationContext.getBean(EmbeddedApplication.class);
                embeddedApplication.start();
            }
            refreshScope = applicationContext.findBean(RefreshScope.class).orElse(null);
        }
    }

    /**
     * Allows subclasses to customize the builder right before context initialization.
     *
     * @param builder the application context builder
     */
    protected void postProcessBuilder(ApplicationContextBuilder builder) {
    }

    /**
     * Resolves any test properties.
     *
     * @param context The test context
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
        if (method != null) {
            if (propertyAnnotations != null && !propertyAnnotations.isEmpty()) {
                for (Property property : propertyAnnotations) {
                    final String name = property.name();
                    oldValues.put(name,
                        testProperties.put(name, property.value())
                    );
                }
            } else {
                testProperties.putAll(oldValues);
            }

            if (testAnnotationValue.rebuildContext()) {
                stopEmbeddedApplication();
                if (applicationContext.isRunning()) {
                    applicationContext.stop();
                }
                applicationContext = builder.build();
                startApplicationContext();
                if (testAnnotationValue.startApplication() && applicationContext.containsBean(EmbeddedApplication.class)) {
                    embeddedApplication = applicationContext.getBean(EmbeddedApplication.class);
                }
                startEmbeddedApplication();
            } else if (!oldValues.isEmpty()) {
                final Map<String, Object> diff = applicationContext.getEnvironment().refreshAndDiff();
                refreshScope.onRefreshEvent(new RefreshEvent(diff));
            }
        }

        if (testInstance != null) {
            if (applicationContext != null) {
                beforeInjectTestInstance(context, testInstance);
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
        stopEmbeddedApplication();
        if (applicationContext != null && applicationContext.isRunning()) {
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
                for (Map.Entry<String, Object> entry : oldValues.entrySet()) {
                    Object value = entry.getValue();
                    if (value != null) {
                        testProperties.put(entry.getKey(), value);
                    } else {
                        testProperties.remove(entry.getKey());
                    }
                }
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
        listeners = new ArrayList<>(applicationContext.getBeansOfType(TestExecutionListener.class));
        Collection collection = applicationContext.getBeansOfType(TestMethodInterceptor.class);
        interceptors = new ArrayList<>(collection);
    }

    /**
     * @param requiredTestClass The test class
     * @return true if the te given class has a bean definition class in the classpath (ie: the annotation processor has been run correctly)
     */
    protected boolean isTestSuiteBeanPresent(Class<?> requiredTestClass) {
        String prefix = requiredTestClass.getPackage().getName() + ".$" + requiredTestClass.getSimpleName();
        final ClassLoader classLoader = requiredTestClass.getClassLoader();
        return ClassUtils.isPresent(prefix + "Definition", classLoader) ||
               ClassUtils.isPresent(prefix + "$Definition", classLoader);
    }

    /**
     * @param context The context
     * @param instance The mock instance to inject
     */
    protected abstract void alignMocks(C context, Object instance);

    /**
     * Allows implementations to inject enclosing or related test instances before the current
     * test instance is injected and mock fields are aligned.
     *
     * @param context The context
     * @param testInstance The current test instance
     */
    protected void beforeInjectTestInstance(C context, Object testInstance) {
    }

    private void startEmbeddedApplication() {
        if (embeddedApplication != null) {
            embeddedApplication.start();
        }
    }

    private void stopEmbeddedApplication() {
        if (embeddedApplication != null) {
            embeddedApplication.stop();
        }
    }

    /**
     * Fires events to the {@link TestExecutionListener}s.
     */
    @FunctionalInterface
    private interface TestListenerCallback {

        void apply(TestExecutionListener listener, TestContext context) throws Exception;

    }

    private record MicronautIntercepted(BeanDefinition<?> specDefinition,
                                        InterceptorRegistry interceptorRegistry,
                                        List<BeanRegistration<Interceptor>> micronautInterceptors) implements TestMethodInterceptor<Object> {

        @Override
        public Object interceptBeforeEach(TestMethodInvocationContext<Object> methodInvocationContext) {
            return intercept(methodInvocationContext);
        }

        @Override
        public Object interceptTest(TestMethodInvocationContext<Object> methodInvocationContext) {
            return intercept(methodInvocationContext);
        }

        @Override
        public Object interceptAfterEach(TestMethodInvocationContext<Object> methodInvocationContext) {
            return intercept(methodInvocationContext);
        }

        private Object intercept(TestMethodInvocationContext<Object> methodInvocationContext) {
            TestContext testContext = methodInvocationContext.getTestContext();
            AnnotatedElement testMethod = testContext.getTestMethod();
            if (testMethod instanceof Method executable) {
                Optional<? extends ExecutableMethod<?, Object>> method = specDefinition.findMethod(executable.getName(), executable.getParameterTypes());
                if (method.isPresent()) {
                    ExecutableMethod<Object, ?> executableMethod = (ExecutableMethod<Object, ?>) method.get();
                    List<BeanRegistration<Interceptor<Object, ?>>> micronautInterceptors1 = (List) micronautInterceptors;
                    Interceptor<Object, ?>[] interceptors = InterceptorChain.resolveAroundInterceptors(interceptorRegistry, executableMethod, micronautInterceptors1);
                    Interceptor<Object, ?> valueResolver = (Interceptor<Object, Object>) context -> {
                        try {
                            return methodInvocationContext.proceed();
                        } catch (Throwable e) {
                            return sneakyThrow(e);
                        }
                    };
                    interceptors = ArrayUtils.concat(interceptors, valueResolver);
                    if (interceptors.length > 0) {
                        // Interceptor doesn't support argument values, no support for altering values
                        return new MethodInterceptorChain(
                            interceptors,
                            testContext.getTestInstance(),
                            executableMethod,
                            new Object[0])
                            .proceed();
                    }

                }
            }
            try {
                return methodInvocationContext.proceed();
            } catch (Throwable e) {
                return sneakyThrow(e);
            }
        }

        @SuppressWarnings("unchecked")
        public static <T extends Throwable, R> R sneakyThrow(Throwable t) throws T {
            throw (T) t;
        }
    }

}
