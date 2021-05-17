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
import io.micronaut.context.annotation.Property;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.FieldInjectionPoint;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.test.annotation.AnnotationUtils;
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
    private static final boolean JAVAX_PRESENT = isTypePresent("javax.inject.Named");
    private static final boolean JAKARTA_PRESENT = isTypePresent("jakarta.inject.Named");

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
        final Optional<io.micronaut.test.annotation.MicronautTest> micronautTest =
                AnnotationSupport.findAnnotation(testClass, io.micronaut.test.annotation.MicronautTest.class);
        return micronautTest
                .map(AnnotationUtils::buildValueObject)
                .orElseGet(() -> AnnotationSupport
                        .findAnnotation(testClass, MicronautTest.class)
                        .map(this::buildValueObject)
                        .orElse(null));
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
        afterClass(extensionContext);
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
        return AnnotationSupport.isAnnotated(testClass, MicronautTest.class) ||
                AnnotationSupport.isAnnotated(testClass, io.micronaut.test.annotation.MicronautTest.class);
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
        return applicationContext != null && applicationContext.containsBean(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (JAVAX_PRESENT) {
            javax.inject.Named named = parameterContext.findAnnotation(javax.inject.Named.class).orElse(null);
            if (named != null) {
                return applicationContext.getBean(parameterContext.getParameter().getType(), Qualifiers.byName(named.value()));
            }
        }
        if (JAKARTA_PRESENT) {
            jakarta.inject.Named named = parameterContext.findAnnotation(jakarta.inject.Named.class).orElse(null);
            if (named != null) {
                return applicationContext.getBean(parameterContext.getParameter().getType(), Qualifiers.byName(named.value()));
            }
        }

        return applicationContext.getBean(parameterContext.getParameter().getType());
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
            List<Object> allInstances = testInstances.getAllInstances();
            allInstances.stream().limit(allInstances.size() - 1).forEach(applicationContext::inject);
        });
    }

    private static boolean isTypePresent(String type) {
        try {
            Class.forName(type);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
