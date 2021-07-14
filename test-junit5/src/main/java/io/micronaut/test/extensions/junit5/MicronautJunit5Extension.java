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
package io.micronaut.test.extensions.junit5;

import io.micronaut.aop.InterceptedProxy;
import io.micronaut.context.ApplicationContext;
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
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.test.annotation.MicronautTestValue;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.context.TestContext;
import io.micronaut.test.extensions.AbstractMicronautExtension;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Extension for JUnit 5.
 *
 * @author graemerocher
 * @since 1.0
 */
public class MicronautJunit5Extension extends AbstractMicronautExtension<ExtensionContext> implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, ExecutionCondition, BeforeTestExecutionCallback, AfterTestExecutionCallback, ParameterResolver, InvocationInterceptor {
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(MicronautJunit5Extension.class);

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        final Class<?> testClass = extensionContext.getRequiredTestClass();
        MicronautTestValue micronautTestValue = buildMicronautTestValue(testClass);
        beforeClass(extensionContext, testClass, micronautTestValue);
        getStore(extensionContext).put(ApplicationContext.class, applicationContext);
        if (specDefinition != null) {
            TestInstance ti = AnnotationSupport.findAnnotation(testClass, TestInstance.class).orElse(null);
            if (ti != null && ti.value() == TestInstance.Lifecycle.PER_CLASS) {
                Object testInstance = extensionContext.getRequiredTestInstance();
                applicationContext.inject(testInstance);
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
        beforeSetupTest(buildContext(extensionContext));
        invocation.proceed();
        afterSetupTest(buildContext(extensionContext));
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        beforeCleanupTest(buildContext(extensionContext));
        invocation.proceed();
        afterCleanupTest(buildContext(extensionContext));
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
        if (specDefinition != null) {
            for (FieldInjectionPoint injectedField : specDefinition.getInjectedFields()) {
                final boolean isMock = applicationContext.resolveMetadata(injectedField.getType()).isAnnotationPresent(MockBean.class);
                if (isMock) {
                    final Field field = injectedField.getField();
                    field.setAccessible(true);
                    try {
                        final Object mock = field.get(instance);
                        if (mock instanceof InterceptedProxy) {
                            InterceptedProxy ip = (InterceptedProxy) mock;
                            final Object target = ip.interceptedTarget();
                            field.set(instance, target);
                        }
                    } catch (IllegalAccessException e) {
                        // continue
                    }
                }
            }
        }
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        afterTestExecution(buildContext(context));
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        beforeTestExecution(buildContext(context));
    }

    private TestContext buildContext(ExtensionContext context) {
      return new TestContext(
          applicationContext,
          context.getTestClass().orElse(null),
          context.getTestMethod().orElse(null),
          context.getTestInstance().orElse(null),
          context.getExecutionException().orElse(null));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
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
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        final Argument<?> argument = getArgument(parameterContext, applicationContext);
        if (argument != null) {
            Optional<String> v = argument.getAnnotationMetadata().stringValue(Value.class);
            if (v.isPresent()) {
                Optional<String> finalV = v;
                return applicationContext.getEnvironment().getProperty(v.get(), argument)
                        .orElseThrow(() ->
                    new ParameterResolutionException("Unresolvable property specified to @Value: " + finalV.get())
                );
            } else {
                v = argument.getAnnotationMetadata().stringValue(Property.class, "name");
                if (v.isPresent()) {
                    Optional<String> finalV1 = v;
                    return applicationContext.getEnvironment()
                            .getProperty(v.get(), argument).orElseThrow(() ->
                                    new ParameterResolutionException("Unresolvable property specified to @Property: " + finalV1.get())
                            );
                } else {
                    return applicationContext.getBean(argument.getType(), resolveQualifier(argument));
                }
            }
        } else {
            return applicationContext.getBean(parameterContext.getParameter().getType());
        }
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
                micronautTest.startApplication());
    }

    private boolean isNestedTestClass(Class<?> testClass) {
        return AnnotationSupport.isAnnotated(testClass, Nested.class);
    }

    private void injectEnclosingTestInstances(ExtensionContext extensionContext) {
        extensionContext.getTestInstances().ifPresent(testInstances -> {
            testInstances.getEnclosingInstances().forEach(applicationContext::inject);
        });
    }

    private Argument<?> getArgument(ParameterContext parameterContext, ApplicationContext applicationContext){
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
     * @param argument The argument
     * @param <T> The type
     * @return The resolved qualifier
     */
    @SuppressWarnings("unchecked")
    private static <T> Qualifier<T> resolveQualifier(Argument<?> argument) {
        AnnotationMetadata annotationMetadata = Objects.requireNonNull(argument, "Argument cannot be null").getAnnotationMetadata();
        boolean hasMetadata = annotationMetadata != AnnotationMetadata.EMPTY_METADATA;

        List<String> qualifierTypes = hasMetadata ? annotationMetadata.getAnnotationNamesByStereotype(AnnotationUtil.QUALIFIER) : null;
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
}
