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
import io.micronaut.context.DefaultBeanResolutionContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.context.env.PropertySourceLoader;
import io.micronaut.context.env.PropertySourcesLocator;
import io.micronaut.core.annotation.AnnotationMetadata;
import org.jspecify.annotations.Nullable;
import io.micronaut.core.io.ResourceResolver;
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
import io.micronaut.inject.InjectableBeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.ProxyBeanDefinition;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.runtime.context.scope.refresh.RefreshEvent;
import io.micronaut.runtime.context.scope.refresh.RefreshScope;
import io.micronaut.test.annotation.AnnotationUtils;
import io.micronaut.test.annotation.MicronautTestValue;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.annotation.Sql;
import io.micronaut.test.condition.TestActiveCondition;
import io.micronaut.test.context.TestContext;
import io.micronaut.test.context.TestExecutionListener;
import io.micronaut.test.context.TestMethodInterceptor;
import io.micronaut.test.context.TestMethodInvocationContext;
import io.micronaut.test.support.TestPropertyProvider;
import io.micronaut.test.support.TestPropertyProviderFactory;
import io.micronaut.test.support.sql.TestSqlAnnotationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Abstract base class for both JUnit 5 and Spock.
 *
 * @param <C> The extension context
 * @author graemerocher
 * @since 1.0
 */
public abstract class AbstractMicronautExtension<C> implements TestExecutionListener, TestMethodInterceptor<Object> {
    public static final String NATIVE_IMAGE_CODE_PROPERTY = "org.graalvm.nativeimage.imagecode";
    /**
     * System property limiting how many test application contexts may be active at the same time.
     */
    public static final String CONTEXT_PARALLELISM_PROPERTY = "micronaut.test.context.parallelism";
    public static final String TEST_ROLLBACK = "micronaut.test.rollback";
    public static final String TEST_TRANSACTIONAL = "micronaut.test.transactional";
    public static final String TEST_TRANSACTION_MODE = "micronaut.test.transaction-mode";
    public static final String TEST_TRANSACTIONAL_DEFAULT = "micronaut.test.transactional-default";
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
    /**
     * The failure reported when a test class is itself a {@code @ClientFilter}.
     */
    public static final String CLIENT_FILTER_MESSAGE = """
        The test class %s is annotated with @ClientFilter. \
        A filter is created as a separate bean, so it would be a second instance of the test class, \
        and its @Client injection points need the filter again, which never ends. \
        Move the filter methods into their own @ClientFilter bean, for example a static nested class annotated with @Requires.""";
    private static final String CLIENT_FILTER_ANNOTATION = "io.micronaut.http.annotation.ClientFilter";
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMicronautExtension.class);
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
    private TestContextParallelismLimiter.Lease contextParallelismLease;
    // true while the context started for the class has not been used by a test yet
    private boolean unusedClassContext;
    private MicronautIntercepted micronautIntercepted;

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

    /**
     * Applies the Micronaut around advice declared on a class level lifecycle method of the test
     * instance, such as a non-static JUnit {@code @BeforeAll} method. {@link TestMethodInterceptor}s
     * are not applied, because they have no class level callbacks.
     *
     * @param methodInvocationContext The invocation context
     * @return The result of the method
     * @throws Throwable if the method fails
     */
    protected Object interceptClassLifecycleMethod(TestMethodInvocationContext<Object> methodInvocationContext) throws Throwable {
        if (micronautIntercepted == null) {
            return methodInvocationContext.proceed();
        }
        return micronautIntercepted.intercept(methodInvocationContext);
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
    public void testDisabled(TestContext testContext, String reason) throws Exception {
        if (listeners != null) {
            for (int i = listeners.size() - 1; i >= 0; i--) {
                listeners.get(i).testDisabled(testContext, reason);
            }
        }
    }

    @Override
    public void testSuccessful(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::testSuccessful, testContext, true);
    }

    @Override
    public void testAborted(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::testAborted, testContext, true);
    }

    @Override
    public void testFailed(TestContext testContext) throws Exception {
        fireListeners(TestExecutionListener::testFailed, testContext, true);
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
     * @param callback    the execution listener callback
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
            if (contextParallelismLease == null) {
                contextParallelismLease = TestContextParallelismLimiter.acquire();
            }
            boolean started = false;
            try {
                startTestContext(context, testClass, testAnnotationValue);
                started = true;
            } finally {
                if (!started) {
                    try {
                        stopTestContext();
                    } finally {
                        releaseContextParallelismLease();
                    }
                }
            }
        }
    }

    private void startTestContext(C context, Class<?> testClass, MicronautTestValue testAnnotationValue) {
        Class<? extends ApplicationContextBuilder>[] cb = testAnnotationValue.contextBuilder();
        if (ArrayUtils.isNotEmpty(cb)) {
            this.builder = InstantiationUtils.instantiate(cb[0]);
        }
        this.testAnnotationValue = testAnnotationValue;
        this.testClass = testClass;

        final Package aPackage = testClass.getPackage();
        builder.packages(aPackage.getName());
        ClassPathResourceLoader classPathResourceLoader = ClassPathResourceLoader.defaultLoader(null);
        ClassLoader classLoader = classPathResourceLoader.getClassLoader();
        builder.resourceResolver(CombinedClassPathResourceLoader.of(
            classPathResourceLoader,
            new ClassClassPathResourceLoader(testClass)
        ));
        final List<Property> ps = AnnotationUtils.findRepeatableAnnotations(testClass, Property.class);
        validateUniquePropertyNames(ps, "test class " + testClass.getName());
        for (Property property : ps) {
            testProperties.put(property.name(), property.value());
        }

        testProperties.put(TestActiveCondition.ACTIVE_SPEC_CLAZZ, testClass);
        testProperties.put(TEST_ROLLBACK, String.valueOf(testAnnotationValue.rollback()));
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
                return loadTestPropertySources(environment, testAnnotationValue, testClass, classLoader);
            }
        });

        postProcessBuilder(builder);
        this.applicationContext = builder.build();
        startApplicationContext();
        resolveFromApplicationContext();
        unusedClassContext = true;
        warnAboutMockBeansWithoutTarget();
        if (specDefinition != null && specDefinition.getAnnotationMetadata().hasStereotype(CLIENT_FILTER_ANNOTATION)) {
            throw new IllegalStateException(CLIENT_FILTER_MESSAGE.formatted(testClass.getName()));
        }
        if (testAnnotationValue.startApplication() && applicationContext.containsBean(EmbeddedApplication.class)) {
            embeddedApplication = applicationContext.getBean(EmbeddedApplication.class);
            embeddedApplication.start();
        }
    }

    private List<PropertySource> loadTestPropertySources(Environment environment, MicronautTestValue testAnnotationValue, Class<?> testClass, ClassLoader classLoader) {
        List<PropertySource> loadedPropertySources = loadAnnotatedPropertySources(environment, testAnnotationValue.propertySources(), testClass);
        applyTestPropertyProviderFactories(environment, loadedPropertySources, testClass);
        applyTransactionalDefault(testAnnotationValue, classLoader);
        if (!testProperties.isEmpty()) {
            loadedPropertySources.add(PropertySource.of(TEST_PROPERTY_SOURCE, testProperties));
        }
        return loadedPropertySources;
    }

    private static List<PropertySource> loadAnnotatedPropertySources(Environment environment, String[] propertySourceNames, Class<?> testClass) {
        List<PropertySource> loadedPropertySources = new ArrayList<>();
        ResourceResolver resourceResolver = new ResourceResolver();
        for (String propertySourceName : propertySourceNames) {
            String ext = NameUtils.extension(propertySourceName);
            if (StringUtils.isEmpty(ext)) {
                continue;
            }
            int loadedBefore = loadedPropertySources.size();
            for (PropertySourceLoader loader : environment.getPropertySourceLoaders()) {
                if (loader.getExtensions().contains(ext)) {
                    resourceResolver.getResourceAsStream(propertySourceName)
                        .or(() -> environment.getResourceAsStream(propertySourceName))
                        .ifPresent(inputStream -> loadedPropertySources.add(readPropertySource(loader, propertySourceName, inputStream)));
                }
            }
            if (loadedPropertySources.size() == loadedBefore) {
                LOG.warn("@MicronautTest property source [{}] of {} was not found. A name without a prefix is looked up at the root of the classpath and next to the test class; use classpath: or file: to be explicit.", propertySourceName, testClass.getName());
            }
        }
        return loadedPropertySources;
    }

    private static PropertySource readPropertySource(PropertySourceLoader loader, String propertySourceName, InputStream inputStream) {
        String filename = NameUtils.filename(propertySourceName);
        try (inputStream) {
            return PropertySource.of(filename, loader.read(filename, inputStream));
        } catch (IOException e) {
            throw new RuntimeException("Error loading property source reference for @MicronautTest: " + filename);
        }
    }

    private void applyTestPropertyProviderFactories(Environment environment, List<PropertySource> loadedPropertySources, Class<?> testClass) {
        List<TestPropertyProviderFactory> testPropertyProviderFactories = SoftServiceLoader.load(TestPropertyProviderFactory.class).collectAll();
        if (testPropertyProviderFactories.isEmpty()) {
            return;
        }
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

    private void applyTransactionalDefault(MicronautTestValue testAnnotationValue, ClassLoader classLoader) {
        Object testTransactionalDefault = testProperties.get(TEST_TRANSACTIONAL_DEFAULT);
        if (testTransactionalDefault != null && !Boolean.parseBoolean(testTransactionalDefault.toString())) {
            if (isExplicitTransactionalValue(classLoader)) {
                testProperties.put(TEST_TRANSACTIONAL, String.valueOf(testAnnotationValue.transactional()));
            } else {
                testProperties.put(TEST_TRANSACTIONAL, Boolean.FALSE.toString());
            }
        } else if (!testProperties.containsKey(TEST_TRANSACTIONAL)) {
            testProperties.put(TEST_TRANSACTIONAL, String.valueOf(testAnnotationValue.transactional()));
        }
    }

    private boolean isExplicitTransactionalValue(ClassLoader classLoader) {
        try {
            Class<?> beanDefinitionClass = classLoader.loadClass(testClass.getPackage().getName() + ".$" + testClass.getSimpleName() + "$Definition");
            BeanDefinition<?> beanDefinition = (BeanDefinition<?>) beanDefinitionClass.getDeclaredConstructor().newInstance();
            return isMicronautTestTransactionalDeclared(beanDefinition);
        } catch (Exception ignore) {
            return false;
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
     * @param context             The test context
     * @param testAnnotationValue The test annotation
     * @param testProperties      The test properties
     */
    protected abstract void resolveTestProperties(C context, MicronautTestValue testAnnotationValue, Map<String, Object> testProperties);

    /**
     * To be called by the different implementations before each test method.
     *
     * @param context             The test context
     * @param testInstance        The test instance
     * @param method              The test method
     * @param propertyAnnotations The {@code @Property} annotations found in the test method, if any
     */
    protected void beforeEach(C context, @Nullable Object testInstance, @Nullable AnnotatedElement method, List<Property> propertyAnnotations) {
        if (method != null) {
            if (testAnnotationValue.rebuildContext()) {
                beforeEachWithRebuild(context, method, propertyAnnotations);
            } else {
                // reverts what a framework without an afterEach callback, such as Kotest, left behind
                boolean reverted = revertMethodProperties();
                applyMethodProperties(method, propertyAnnotations);
                if ((reverted || !oldValues.isEmpty()) && refreshScope != null) {
                    final Map<String, Object> diff = applicationContext.getEnvironment().refreshAndDiff();
                    refreshScope.onRefreshEvent(new RefreshEvent(diff));
                }
            }
        }

        if (testInstance != null && applicationContext != null) {
            beforeInjectTestInstance(context, testInstance);
            if (refreshScope != null) {
                refreshScope.onRefreshEvent(new RefreshEvent(Collections.singletonMap(
                    TestActiveCondition.ACTIVE_MOCKS, "changed"
                )));
            }
            injectTestInstance(testInstance);
            alignMocks(context, testInstance);
        }
    }

    /**
     * Injects an instance of the test class, or of a class nested in it, from the running context.
     *
     * @param testInstance The test instance
     */
    protected void injectTestInstance(Object testInstance) {
        // specDefinition is the outermost test class's, which a nested class's instance is not an instance of
        if (specDefinition instanceof ProxyBeanDefinition<?> proxyDefinition
            && proxyDefinition.getTargetType().isInstance(testInstance)
            && specDefinition instanceof InjectableBeanDefinition<?> injectableBeanDefinition) {
            // Proxy bean is not going to resolve bean definition, we need a small hack
            injectInto(injectableBeanDefinition, testInstance);
        } else {
            applicationContext.inject(testInstance);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void injectInto(InjectableBeanDefinition<T> definition, Object testInstance) {
        definition.inject(new DefaultBeanResolutionContext(applicationContext, definition), applicationContext, (T) testInstance);
    }

    private void applyMethodProperties(AnnotatedElement method, @Nullable List<Property> propertyAnnotations) {
        if (propertyAnnotations != null && !propertyAnnotations.isEmpty()) {
            validateUniquePropertyNames(propertyAnnotations, "test method " + method);
            for (Property property : propertyAnnotations) {
                final String name = property.name();
                oldValues.put(name,
                    testProperties.put(name, property.value())
                );
            }
        }
    }

    private void beforeEachWithRebuild(C context, AnnotatedElement method, @Nullable List<Property> propertyAnnotations) {
        boolean hasMethodProperties = propertyAnnotations != null && !propertyAnnotations.isEmpty();
        if (hasMethodProperties) {
            // rejected before the running context is touched, so that the next test can still use it
            validateUniquePropertyNames(propertyAnnotations, "test method " + method);
        }
        // the context started for the class is already fresh, so the first test uses it instead of
        // starting a second one, unless it needs its own method-level properties
        boolean rebuild = !unusedClassContext || hasMethodProperties;
        unusedClassContext = false;
        if (rebuild) {
            stopContextForRebuild();
        }
        // the previous test's method-level properties stay in force until its context has stopped
        revertMethodProperties();
        applyMethodProperties(method, propertyAnnotations);
        if (rebuild) {
            startRebuiltContext(context);
        }
    }

    private void stopContextForRebuild() {
        stopEmbeddedApplication();
        if (applicationContext.isRunning()) {
            applicationContext.stop();
        }
        // Everything resolved from the stopped context is stale: drop it before the
        // new context starts, so that nothing can reach the old context through it.
        embeddedApplication = null;
        refreshScope = null;
        specDefinition = null;
    }

    private void startRebuiltContext(C context) {
        applicationContext = builder.build();
        startApplicationContext();
        resolveFromApplicationContext();
        if (testAnnotationValue.startApplication() && applicationContext.containsBean(EmbeddedApplication.class)) {
            embeddedApplication = applicationContext.getBean(EmbeddedApplication.class);
        }
        startEmbeddedApplication();
        applicationContextRebuilt(context);
    }

    private void validateUniquePropertyNames(List<Property> properties, String propertySourceDescription) {
        Set<String> names = new HashSet<>(properties.size());
        for (Property property : properties) {
            String name = property.name();
            if (!names.add(name)) {
                throw new IllegalStateException("Duplicate @Property name [" + name + "] declared on " + propertySourceDescription);
            }
        }
    }

    /**
     * Executed after the class is complete.
     *
     * @param context the context
     */
    protected void afterClass(C context) {
        try {
            stopTestContext();
        } finally {
            releaseContextParallelismLease();
        }
    }

    private void stopTestContext() {
        stopEmbeddedApplication();
        if (applicationContext != null && applicationContext.isRunning()) {
            applicationContext.stop();
        }
        embeddedApplication = null;
        applicationContext = null;
        refreshScope = null;
        specDefinition = null;
        micronautIntercepted = null;
        listeners = Collections.emptyList();
        interceptors = Collections.emptyList();
        // left by the last test under rebuildContext, now that its context has shut down
        revertMethodProperties();
    }

    /**
     * Executed after each test completes.
     *
     * @param context The context
     * @throws Exception allows any exception to propagate
     */
    public void afterEach(C context) throws Exception {
        if (oldValues.isEmpty()) {
            return;
        }
        if (testAnnotationValue != null && testAnnotationValue.rebuildContext()) {
            // This test's context is still running and is replaced before the next test, so its
            // method-level properties stay in force until it has shut down. They are reverted
            // before the next context is built, or when the class finishes.
            return;
        }
        revertMethodProperties();
        if (refreshScope != null) {
            final Map<String, Object> diff = applicationContext.getEnvironment().refreshAndDiff();
            refreshScope.onRefreshEvent(new RefreshEvent(diff));
        }
    }

    private boolean revertMethodProperties() {
        boolean reverted = !oldValues.isEmpty();
        for (Map.Entry<String, Object> entry : oldValues.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                testProperties.put(entry.getKey(), value);
            } else {
                testProperties.remove(entry.getKey());
            }
        }
        oldValues.clear();
        return reverted;
    }

    private static boolean isMicronautTestTransactionalDeclared(AnnotationMetadata metadata) {
        for (String annotationName : metadata.getAnnotationNames()) {
            if (!annotationName.endsWith(".MicronautTest")) {
                continue;
            }
            Map<CharSequence, Object> declaredValues = metadata.getValues(annotationName);
            if (declaredValues.containsKey("transactional")) {
                return true;
            }
        }
        return false;
    }

    /**
     * A {@code @MockBean} without a value does not replace anything, so a bean of the same type
     * keeps being injected and the mock is silently ignored. Say so.
     */
    private void warnAboutMockBeansWithoutTarget() {
        try {
            for (BeanDefinition<?> mock : applicationContext.getBeanDefinitions(Qualifiers.byStereotype(MockBean.class))) {
                warnIfMockBeanIsIgnored(mock);
            }
        } catch (RuntimeException e) {
            // only a diagnostic: its lookups load beans the test may never use, which must not fail the test
            LOG.debug("Could not check the @MockBean declarations of {}", testClass.getName(), e);
        }
    }

    private void warnIfMockBeanIsIgnored(BeanDefinition<?> mock) {
        AnnotationMetadata metadata = mock.getAnnotationMetadata();
        // @MockBean's members default to void.class and "", which declare nothing
        boolean declaresBean = metadata.classValue(Replaces.class).filter(type -> type != void.class).isPresent()
            || metadata.stringValue(Replaces.class, "named").filter(name -> !name.isEmpty()).isPresent();
        if (declaresBean || mock.isPrimary()) {
            return;
        }
        Class<?> mockType = beanClass(mock);
        List<String> others = findBeansCompetingWithMock(mock, mockType);
        if (!others.isEmpty()) {
            LOG.warn("@MockBean of type {} in {} does not declare the bean it replaces, so {} can still be injected instead of the mock. Declare the bean to replace as the value of @MockBean.",
                mockType.getName(), testClass.getName(), others);
        }
    }

    /**
     * Only a bean with a compatible generic type and the same qualifier can take the place of a mock
     * at the injection point the mock was declared for, and a {@code @Secondary} bean gives way to it.
     */
    private List<String> findBeansCompetingWithMock(BeanDefinition<?> mock, Class<?> mockType) {
        return typesReplacedByMock(mockType).stream()
            .flatMap(type -> findBeansOfMockedType(mock, type).stream())
            .filter(candidate -> !candidate.getAnnotationMetadata().hasStereotype(MockBean.class))
            .filter(candidate -> !candidate.getAnnotationMetadata().hasStereotype(Secondary.class))
            .<String>map(candidate -> beanClass(candidate).getName())
            .distinct()
            .toList();
    }

    @SuppressWarnings("unchecked")
    private <T> List<BeanDefinition<T>> findBeansOfMockedType(BeanDefinition<T> mock, Class<?> mockedType) {
        Argument<T> type = (Argument<T>) Argument.of(mockedType, mock.getTypeArguments(mockedType).toArray(new Argument<?>[0]));
        return applicationContext.getBeanDefinitions(type, mock.getDeclaredQualifier()).stream()
            .filter(candidate -> candidate.isCandidateBean(type))
            .toList();
    }

    /**
     * A mock declared on a method mocks the method's return type, while a mock declared on a class
     * stands in for the types that class extends.
     */
    private static List<Class<?>> typesReplacedByMock(Class<?> mockType) {
        if (!mockType.isAnnotationPresent(MockBean.class)) {
            return List.of(mockType);
        }
        List<Class<?>> types = new ArrayList<>(List.of(mockType.getInterfaces()));
        if (mockType.getSuperclass() != null && mockType.getSuperclass() != Object.class) {
            types.add(mockType.getSuperclass());
        }
        return types;
    }

    private static Class<?> beanClass(BeanDefinition<?> definition) {
        // an advised bean's type is the generated proxy, which means nothing to the reader
        return definition instanceof ProxyBeanDefinition<?> proxy ? proxy.getTargetType() : definition.getBeanType();
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
     * Resolves the state the extension keeps from the application context: the test's bean
     * definition, the Micronaut interceptors around its methods and the {@link RefreshScope}.
     * Called after every start of a context, including one rebuilt for
     * {@code rebuildContext = true}, so that none of it outlives the context it came from.
     */
    private void resolveFromApplicationContext() {
        specDefinition = applicationContext.findBeanDefinition(testClass).orElse(null);
        micronautIntercepted = null;
        if (specDefinition instanceof ProxyBeanDefinition<?>) {
            micronautIntercepted = new MicronautIntercepted(
                specDefinition,
                applicationContext.getBean(InterceptorRegistry.class),
                new ArrayList<>(applicationContext.getBeanRegistrations(Argument.of(Interceptor.class), null))
            );
            interceptors = new ArrayList<>(interceptors);
            interceptors.add(micronautIntercepted);
        }
        refreshScope = applicationContext.findBean(RefreshScope.class).orElse(null);
    }

    /**
     * Called after {@code rebuildContext = true} replaced the application context before a test,
     * once the new context, and its embedded application if any, have started. Subclasses that
     * keep state from the previous context, beyond what this class resolves again, refresh it here.
     *
     * @param context The test context
     * @since 5.2.1
     */
    protected void applicationContextRebuilt(C context) {
    }

    /**
     * @param requiredTestClass The test class
     * @return true if the te given class has a bean definition class in the classpath (ie: the annotation processor has been run correctly)
     */
    protected boolean isTestSuiteBeanPresent(Class<?> requiredTestClass) {
        if (isNativeImageExecution()) {
            return true;
        }
        String prefix = requiredTestClass.getPackage().getName() + ".$" + requiredTestClass.getSimpleName();
        final ClassLoader classLoader = requiredTestClass.getClassLoader();
        return ClassUtils.isPresent(prefix + "Definition", classLoader) ||
            ClassUtils.isPresent(prefix + "$Definition", classLoader);
    }

    /**
     * Whether the tests run inside a native image, where generated bean definition classes cannot be
     * looked up by name. Override to change how a native image is detected.
     *
     * @return true if running inside a native image
     */
    protected boolean isNativeImageExecution() {
        return StringUtils.isNotEmpty(System.getProperty(NATIVE_IMAGE_CODE_PROPERTY));
    }

    /**
     * @param context  The context
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

    private void releaseContextParallelismLease() {
        if (contextParallelismLease != null) {
            contextParallelismLease.release();
            contextParallelismLease = null;
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
            if (testMethod instanceof Method executable && isInstanceOfTestBean(testContext.getTestInstance())) {
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

        /**
         * The advice is that of the outermost test class's methods, which a method of a nested
         * class with the same name and parameters must not pick up.
         */
        private boolean isInstanceOfTestBean(@Nullable Object testInstance) {
            return testInstance == null
                || !(specDefinition instanceof ProxyBeanDefinition<?> proxyDefinition)
                || proxyDefinition.getTargetType().isInstance(testInstance);
        }

        @SuppressWarnings("unchecked")
        public static <T extends Throwable, R> R sneakyThrow(Throwable t) throws T {
            throw (T) t;
        }
    }

}
