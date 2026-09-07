/*
 * Copyright 2017-2023 original authors
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
package io.micronaut.test.extensions.junit5;

import io.micronaut.aop.Intercepted;
import io.micronaut.aop.InterceptedProxy;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.DefaultBeanResolutionContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.FieldInjectionPoint;
import io.micronaut.inject.InjectableBeanDefinition;
import io.micronaut.inject.MethodInjectionPoint;
import io.micronaut.inject.ProxyBeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.test.annotation.MicronautTestValue;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.context.TestContext;
import io.micronaut.test.context.TestMethodInvocationContext;
import io.micronaut.test.extensions.AbstractMicronautExtension;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Extension for JUnit 5.
 *
 * @author graemerocher
 * @since 1.0
 */
public class MicronautJunit5Extension extends AbstractMicronautExtension<ExtensionContext> implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, ExecutionCondition, BeforeTestExecutionCallback, AfterTestExecutionCallback, ParameterResolver, InvocationInterceptor, TestInstancePreDestroyCallback, TestWatcher {
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(MicronautJunit5Extension.class);
    private static final String TEST_PROPERTY_PROVIDER_LIFECYCLE_MESSAGE = "Tests that implement TestPropertyProvider must use the PER_CLASS test instance lifecycle.";
    private static final String NESTED_CONFIGURATION_MESSAGE = """
        %s cannot be declared on the @Nested class %s. \
        A nested class shares the application context of its enclosing class %s, so this \
        configuration would be ignored rather than applied. \
        Move it to %s, or make %s a top-level test class with its own @MicronautTest.""";

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        final Class<?> testClass = extensionContext.getRequiredTestClass();
        if (isNestedTestClass(testClass)) {
            validateNestedTestClass(testClass);
        }
        final TestInstance.Lifecycle testInstanceLifecycle = extensionContext.getTestInstanceLifecycle().orElse(TestInstance.Lifecycle.PER_METHOD);
        if (TestPropertyProvider.class.isAssignableFrom(testClass)) {
            if (testInstanceLifecycle != TestInstance.Lifecycle.PER_CLASS) {
                throw new ExtensionConfigurationException(TEST_PROPERTY_PROVIDER_LIFECYCLE_MESSAGE);
            }
            extensionContext.getRequiredTestInstance();
        }
        MicronautTestValue micronautTestValue = buildMicronautTestValue(testClass);
        beforeClass(extensionContext, testClass, micronautTestValue);
        getStore(extensionContext).put(ApplicationContext.class, applicationContext);
        if (specDefinition != null) {
            if (testInstanceLifecycle == TestInstance.Lifecycle.PER_CLASS) {
                Object testInstance = extensionContext.getRequiredTestInstance();
                if (specDefinition instanceof ProxyBeanDefinition<?>) {
                    // Proxy bean is not going to resolve bean definition, we need a small hack
                    if (specDefinition instanceof InjectableBeanDefinition injectableBeanDefinition) {
                        injectableBeanDefinition.inject(
                            new DefaultBeanResolutionContext(applicationContext, injectableBeanDefinition),
                            applicationContext,
                            testInstance);
                    }
                } else {
                    applicationContext.inject(testInstance);
                }
            }
        }
        beforeTestClass(buildContext(extensionContext));
    }

    /**
     * Builds a {@link MicronautTestValue} object from the provided class (e.g. by scanning annotations).
     *
     * @param testClass the class to extract builder configuration from
     * @return a MicronautTestValue to configure the test application context
     */
    protected MicronautTestValue buildMicronautTestValue(Class<?> testClass) {
        return AnnotationSupport
            .findAnnotation(testClass, MicronautTest.class)
            .map(this::buildValueObject)
            .orElse(null);
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        TestContext testContext = buildContext(invocationContext);
        beforeSetupTest(testContext);
        interceptBeforeEach(new TestMethodInvocationContext<>() {
            @Override
            public TestContext getTestContext() {
                return testContext;
            }

            @Override
            public Object proceed() throws Throwable {
                return invocation.proceed();
            }
        });
        afterSetupTest(testContext);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        interceptTest(new TestMethodInvocationContext<>() {
            TestContext testContext;

            @Override
            public TestContext getTestContext() {
                if (testContext == null) {
                    testContext = buildContext(extensionContext);
                }
                return testContext;
            }

            @Override
            public Object proceed() throws Throwable {
                return invocation.proceed();
            }

        });
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        interceptTest(new TestMethodInvocationContext<>() {
            TestContext testContext;

            @Override
            public TestContext getTestContext() {
                if (testContext == null) {
                    testContext = buildContext(extensionContext);
                }
                return testContext;
            }

            @Override
            public Object proceed() throws Throwable {
                return invocation.proceed();
            }
        });
    }

    @Override
    public <T> T interceptTestFactoryMethod(Invocation<T> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        return (T) interceptTest(new TestMethodInvocationContext<>() {
            TestContext testContext;

            @Override
            public TestContext getTestContext() {
                if (testContext == null) {
                    testContext = buildContext(extensionContext);
                }
                return testContext;
            }

            @Override
            public Object proceed() throws Throwable {
                return invocation.proceed();
            }
        });
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        TestContext testContext = buildContext(invocationContext);
        beforeCleanupTest(testContext);
        interceptAfterEach(new TestMethodInvocationContext<Object>() {
            @Override
            public TestContext getTestContext() {
                return testContext;
            }

            @Override
            public Object proceed() throws Throwable {
                return invocation.proceed();
            }
        });
        afterCleanupTest(testContext);
    }

    /**
     * A nested class reuses the extension instance, application context and configuration of its
     * outermost enclosing class, so configuration declared on the nested class itself has nowhere
     * to go. Rather than ignoring it, say so.
     *
     * @param testClass the nested test class
     */
    private void validateNestedTestClass(Class<?> testClass) {
        Class<?> enclosingClass = testClass.getEnclosingClass();
        if (enclosingClass == null) {
            return;
        }
        String annotation = null;
        if (testClass.getDeclaredAnnotation(MicronautTest.class) != null) {
            annotation = "@MicronautTest";
        } else if (testClass.getDeclaredAnnotationsByType(Property.class).length > 0) {
            annotation = "@Property";
        }
        if (annotation != null) {
            throw new ExtensionConfigurationException(NESTED_CONFIGURATION_MESSAGE.formatted(
                annotation,
                testClass.getName(),
                enclosingClass.getName(),
                enclosingClass.getSimpleName(),
                testClass.getSimpleName()));
        }
    }

    @Override
    public void preDestroyTestInstance(ExtensionContext extensionContext) {
        if (applicationContext == null || !applicationContext.isRunning()) {
            return;
        }
        extensionContext.getTestInstance().ifPresent(applicationContext::destroyBean);
    }

    @Override
    public void testDisabled(ExtensionContext extensionContext, Optional<String> reason) {
        fireOutcome(extensionContext, null, context -> testDisabled(context, reason.orElse(null)));
    }

    @Override
    public void testSuccessful(ExtensionContext extensionContext) {
        fireOutcome(extensionContext, null, context -> testSuccessful(context));
    }

    @Override
    public void testAborted(ExtensionContext extensionContext, Throwable cause) {
        fireOutcome(extensionContext, cause, context -> testAborted(context));
    }

    @Override
    public void testFailed(ExtensionContext extensionContext, Throwable cause) {
        fireOutcome(extensionContext, cause, context -> testFailed(context));
    }

    /**
     * {@link TestWatcher} callbacks cannot throw a checked exception, and are also reached for tests
     * that never started a context - a disabled class never runs {@code beforeAll}.
     *
     * @param extensionContext the extension context
     * @param cause            the throwable that ended the test, if any
     * @param callback         the listener callback to fire
     */
    private void fireOutcome(ExtensionContext extensionContext, Throwable cause, OutcomeCallback callback) {
        if (applicationContext == null) {
            return;
        }
        try {
            callback.apply(buildContext(extensionContext, cause));
        } catch (Exception e) {
            throw new ExtensionConfigurationException("Error firing test outcome to a TestExecutionListener", e);
        }
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        afterTestClass(buildContext(extensionContext));
        if (!extensionContext.getTestClass().filter(this::isNestedTestClass).isPresent()) {
            afterClass(extensionContext);
        }
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        injectEnclosingTestInstances(extensionContext);
        final Optional<Object> testInstance = extensionContext.getTestInstance();
        final Optional<? extends AnnotatedElement> testMethod = extensionContext.getTestMethod();
        List<Property> propertyAnnotations = null;
        if (testMethod.isPresent()) {
            Property[] annotationsByType = testMethod.get().getAnnotationsByType(Property.class);
            propertyAnnotations = Arrays.asList(annotationsByType);
        }
        beforeEach(extensionContext, testInstance.orElse(null), testMethod.orElse(null), propertyAnnotations);
        beforeTestMethod(buildContext(extensionContext));
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        super.afterEach(extensionContext);
        afterTestMethod(buildContext(extensionContext));
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        final Optional<Object> testInstance = extensionContext.getTestInstance();
        if (testInstance.isPresent()) {

            final Class<?> requiredTestClass = extensionContext.getRequiredTestClass();
            if (applicationContext.containsBean(requiredTestClass) || isNestedTestClass(requiredTestClass)) {
                return ConditionEvaluationResult.enabled("Test bean active");
            } else {

                final boolean hasBeanDefinition = isTestSuiteBeanPresent(requiredTestClass);
                if (!hasBeanDefinition) {
                    throw new TestInstantiationException(MISCONFIGURED_MESSAGE);
                } else {
                    return ConditionEvaluationResult.disabled(DISABLED_MESSAGE);
                }

            }
        } else {
            final Class<?> testClass = extensionContext.getRequiredTestClass();

            // see https://github.com/micronaut-projects/micronaut-test/issues/640
            if (Intercepted.class.isAssignableFrom(testClass)) {
                return ConditionEvaluationResult.disabled("Intercepted Class is not a test");
            }

            if (hasExpectedAnnotations(testClass) || isNestedTestClass(testClass)) {
                return ConditionEvaluationResult.enabled("Test bean active");
            } else {
                return ConditionEvaluationResult.disabled(DISABLED_MESSAGE);
            }
        }
    }

    /**
     * @param testClass the test class
     * @return true if the provided test class holds the expected test annotations
     */
    protected boolean hasExpectedAnnotations(Class<?> testClass) {
        return AnnotationSupport.isAnnotated(testClass, MicronautTest.class);
    }

    @Override
    protected void resolveTestProperties(ExtensionContext context, MicronautTestValue testAnnotationValue, Map<String, Object> testProperties) {
        Object o = context.getTestInstance().orElse(null);
        if (o instanceof TestPropertyProvider) {
            Map<String, String> properties = ((TestPropertyProvider) o).getProperties();
            if (CollectionUtils.isNotEmpty(properties)) {
                testProperties.putAll(properties);
            }
        }
    }

    @Override
    protected void alignMocks(ExtensionContext context, Object instance) {
        if (specDefinition == null) {
            return;
        }
        findSpecInstance(context).ifPresent(specInstance -> {
            for (FieldInjectionPoint injectedField : specDefinition.getInjectedFields()) {
                if (applicationContext.resolveMetadata(injectedField.getType()).isAnnotationPresent(MockBean.class)) {
                    findField(specInstance.getClass(), injectedField.getName()).ifPresent(field -> alignMock(specInstance, field));
                }
            }
            for (MethodInjectionPoint<?, ?> injectedMethod : specDefinition.getInjectedMethods()) {
                final Argument<?>[] arguments = injectedMethod.getArguments();
                if (arguments.length == 1 && applicationContext.resolveMetadata(arguments[0].getType()).isAnnotationPresent(MockBean.class)) {
                    findMethodInjectedField(specInstance.getClass(), injectedMethod, arguments[0])
                        .ifPresent(field -> alignMock(specInstance, field));
                }
            }
        });
    }

    private void alignMock(Object specInstance, Field field) {
        field.setAccessible(true);
        try {
            final Object mock = field.get(specInstance);
            if (mock instanceof InterceptedProxy) {
                field.set(specInstance, ((InterceptedProxy) mock).interceptedTarget());
            }
        } catch (IllegalAccessException e) {
            // continue
        }
    }

    private Optional<Field> findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null) {
            try {
                return Optional.of(current.getDeclaredField(name));
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return Optional.empty();
    }

    private Optional<Field> findMethodInjectedField(Class<?> type, MethodInjectionPoint<?, ?> injectedMethod, Argument<?> argument) {
        final String methodName = injectedMethod.getName();
        if (methodName.startsWith("set") && methodName.length() > 3) {
            final String fieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
            final Optional<Field> field = findField(type, fieldName);
            if (field.isPresent()) {
                return field;
            }
        }
        return findField(type, argument.getName());
    }

    private Optional<?> findSpecInstance(ExtensionContext context) {
        return context.getTestInstances()
            .flatMap(testInstances -> testInstances.findInstance(specDefinition.getBeanType()));
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        afterTestExecution(buildContext(context));
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        beforeTestExecution(buildContext(context));
    }

    private TestContext buildContext(ReflectiveInvocationContext<?> context) {
        return new TestContext(
            applicationContext,
            context.getTargetClass(),
            context.getExecutable(),
            context.getTarget(),
            null,
            context.getExecutable().getName(),
            true);
    }

    private TestContext buildContext(ExtensionContext context) {
        return buildContext(context, null);
    }

    private TestContext buildContext(ExtensionContext context, Throwable cause) {
        return new TestContext(
            applicationContext,
            context.getTestClass().orElse(null),
            context.getTestMethod().orElse(null),
            context.getTestInstance().orElse(null),
            cause != null ? cause : context.getExecutionException().orElse(null),
            context.getDisplayName(),
            true);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (this.testAnnotationValue != null) {
            if (!this.testAnnotationValue.isResolveParameters() && parameterContext.getDeclaringExecutable() instanceof Method) {
                return false;
            }

            if (isApplicationContextParameter(parameterContext)) {
                return true;
            }

            final Argument<?> argument = getArgument(parameterContext, applicationContext);
            if (argument != null) {
                if (argument.isAnnotationPresent(Value.class) || argument.isAnnotationPresent(Property.class)) {
                    return true;
                } else {
                    return applicationContext.containsBean(argument.getType(), resolveQualifier(argument));
                }
            } else {
                return applicationContext.containsBean(parameterContext.getParameter().getType());
            }
        } else {
            return false;
        }
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (isApplicationContextParameter(parameterContext)) {
            return applicationContext;
        }

        final Argument<?> argument = getArgument(parameterContext, applicationContext);
        if (argument != null) {
            Optional<String> v = argument.getAnnotationMetadata().stringValue(Value.class);
            if (v.isPresent()) {
                return resolveValueParameter(parameterContext, argument, v.get());
            } else {
                v = argument.getAnnotationMetadata().stringValue(Property.class, "name");
                if (v.isPresent()) {
                    Optional<String> finalV1 = v;
                    return applicationContext.getEnvironment()
                        .getProperty(v.get(), argument).orElseThrow(() ->
                            new ParameterResolutionException("Unresolvable property specified to @Property: " + finalV1.get())
                        );
                } else {
                    return applicationContext.getBean(argument, resolveQualifier(argument));
                }
            }
        } else {
            return applicationContext.getBean(parameterContext.getParameter().getType());
        }
    }

    private Object resolveValueParameter(ParameterContext parameterContext, Argument<?> argument, String value) {
        if (argument.getAnnotationMetadata().hasEvaluatedExpressions()) {
            Object resolved = argument.getAnnotationMetadata().getValue(Value.class, argument).orElse(null);
            if (resolved != null || argument.isDeclaredNullable()) {
                return resolved;
            }
            throw new ParameterResolutionException("Unresolvable property specified to @Value: " + value);
        }
        BeanDefinition<?> beanDefinition = applicationContext
            .findBeanDefinition(parameterContext.getDeclaringExecutable().getDeclaringClass())
            .orElse(null);
        if (beanDefinition == null) {
            beanDefinition = specDefinition;
        }
        if (beanDefinition != null) {
            try (DefaultBeanResolutionContext resolutionContext = new DefaultBeanResolutionContext(applicationContext, beanDefinition)) {
                return resolutionContext.resolvePropertyValue(argument, value, null, true);
            } catch (RuntimeException e) {
                throw new ParameterResolutionException("Unresolvable property specified to @Value: " + value, e);
            }
        }
        return applicationContext.resolvePlaceholders(value)
            .flatMap(resolved -> applicationContext.getConversionService().convert(resolved, argument))
            .orElseThrow(() -> new ParameterResolutionException("Unresolvable property specified to @Value: " + value));
    }

    /**
     * @param context the current extension context
     * @return the store to use for this extension
     */
    protected ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getRoot().getStore(NAMESPACE);
    }

    private MicronautTestValue buildValueObject(MicronautTest micronautTest) {
        return new MicronautTestValue(
            micronautTest.application(),
            micronautTest.environments(),
            micronautTest.packages(),
            micronautTest.propertySources(),
            micronautTest.rollback(),
            micronautTest.transactional(),
            micronautTest.rebuildContext(),
            micronautTest.contextBuilder(),
            micronautTest.transactionMode(),
            micronautTest.startApplication(),
            micronautTest.resolveParameters(),
            micronautTest.deduceEnvironment());
    }

    private boolean isNestedTestClass(Class<?> testClass) {
        return AnnotationSupport.isAnnotated(testClass, Nested.class);
    }

    private void injectEnclosingTestInstances(ExtensionContext extensionContext) {
        extensionContext.getTestInstances().ifPresent(testInstances -> {
            testInstances.getEnclosingInstances().forEach(applicationContext::inject);
        });
    }

    private boolean isApplicationContextParameter(ParameterContext parameterContext) {
        return ApplicationContext.class.isAssignableFrom(parameterContext.getParameter().getType());
    }

    private Argument<?> getArgument(ParameterContext parameterContext, ApplicationContext applicationContext) {
        try {
            final Executable declaringExecutable = parameterContext.getDeclaringExecutable();
            final int index = parameterContext.getIndex();
            if (declaringExecutable instanceof Constructor) {
                final Class<?> declaringClass = declaringExecutable.getDeclaringClass();
                final BeanDefinition<?> beanDefinition = applicationContext.findBeanDefinition(declaringClass).orElse(null);
                if (beanDefinition != null) {
                    final Argument<?>[] arguments = beanDefinition.getConstructor().getArguments();
                    if (index < arguments.length) {
                        return arguments[index];
                    }
                }
            } else {

                final ExecutableMethod<?, Object> executableMethod = applicationContext.getExecutableMethod(
                    declaringExecutable.getDeclaringClass(),
                    declaringExecutable.getName(),
                    declaringExecutable.getParameterTypes()
                );
                final Argument<?>[] arguments = executableMethod.getArguments();
                if (index < arguments.length) {
                    return arguments[index];
                }
            }
        } catch (NoSuchMethodException e) {
            return null;
        }
        return null;
    }


    /**
     * Build a qualifier for the given argument.
     *
     * @param argument The argument
     * @param <T>      The type
     * @return The resolved qualifier
     */
    @SuppressWarnings("unchecked")
    private static <T> Qualifier<T> resolveQualifier(Argument<?> argument) {
        AnnotationMetadata annotationMetadata = Objects.requireNonNull(argument, "Argument cannot be null").getAnnotationMetadata();
        boolean hasMetadata = annotationMetadata != AnnotationMetadata.EMPTY_METADATA;

        List<String> qualifierTypes = hasMetadata ? annotationMetadata.getAnnotationNamesByStereotype(AnnotationUtil.QUALIFIER) : Collections.emptyList();
        if (CollectionUtils.isNotEmpty(qualifierTypes)) {
            if (qualifierTypes.size() == 1) {
                return Qualifiers.byAnnotation(
                    annotationMetadata,
                    qualifierTypes.iterator().next()
                );
            } else {
                final Qualifier[] qualifiers = qualifierTypes
                    .stream().map((type) -> Qualifiers.byAnnotation(annotationMetadata, type))
                    .toArray(Qualifier[]::new);
                return Qualifiers.<T>byQualifiers(
                    qualifiers
                );
            }
        }
        return null;
    }

    /**
     * A single {@link io.micronaut.test.context.TestExecutionListener} outcome callback.
     */
    @FunctionalInterface
    private interface OutcomeCallback {

        void apply(TestContext testContext) throws Exception;
    }
}
