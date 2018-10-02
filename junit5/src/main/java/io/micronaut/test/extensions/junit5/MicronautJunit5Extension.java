package io.micronaut.test.extensions.junit5;

import io.micronaut.aop.InterceptedProxy;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.annotation.Property;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.FieldInjectionPoint;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.runtime.context.scope.refresh.RefreshEvent;
import io.micronaut.runtime.context.scope.refresh.RefreshScope;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.condition.TestActiveCondition;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class MicronautJunit5Extension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, ExecutionCondition {

    private Map<String, Object> testProperties = new LinkedHashMap<>();
    private ApplicationContext applicationContext;
    private BeanDefinition<?> specDefinition;
    private EmbeddedApplication embeddedApplication;
    private RefreshScope refreshScope;
    private Map<String, Object> oldValues = new LinkedHashMap<>();

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        final Class<?> testClass = extensionContext.getRequiredTestClass();
        final MicronautTest micronautTest = testClass.getAnnotation(MicronautTest.class);
        if (micronautTest != null) {

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
            final Class<?> application = micronautTest.application();
            if (application != void.class) {
                builder.mainClass(application);
            }

            builder.propertySources(io.micronaut.context.env.PropertySource.of(testProperties));
            this.applicationContext = builder.build();
            applicationContext.start();
            specDefinition = applicationContext.findBeanDefinition(extensionContext.getRequiredTestClass()).orElse(null);
            if (applicationContext.containsBean(EmbeddedApplication.class)) {
                embeddedApplication = applicationContext.getBean(EmbeddedApplication.class);
                embeddedApplication.start();
            }
            refreshScope = applicationContext.findBean(RefreshScope.class).orElse(null);
        }
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        if (embeddedApplication != null) {
            embeddedApplication.stop();
        } else if (applicationContext != null) {
            applicationContext.stop();
        }
        embeddedApplication = null;
        applicationContext = null;
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        final Optional<Object> testInstance = extensionContext.getTestInstance();
        final Optional<? extends AnnotatedElement> testMethod = extensionContext.getTestMethod();
        testMethod.ifPresent(method -> {
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
        });
        testInstance.ifPresent(o -> {
            if (applicationContext != null) {
                if (refreshScope != null) {
                    refreshScope.onApplicationEvent(new RefreshEvent(Collections.singletonMap(
                            TestActiveCondition.ACTIVE_MOCKS, "changed"
                    )));
                }
                applicationContext.inject(o);
                alignMocks(o);
            }
        });

    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        if (!oldValues.isEmpty()) {
            testProperties.putAll(oldValues);
            final Map<String, Object> diff = applicationContext.getEnvironment().refreshAndDiff();
            refreshScope.onApplicationEvent(new RefreshEvent(diff));
        }
        oldValues.clear();
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        final Optional<Object> testInstance = extensionContext.getTestInstance();
        if (testInstance.isPresent()) {

            final Class<?> requiredTestClass = extensionContext.getRequiredTestClass();
            if (applicationContext.containsBean(requiredTestClass)) {
                return ConditionEvaluationResult.enabled("Test bean active");
            } else {
                return ConditionEvaluationResult.disabled("Test does not meet bean requirements");
            }
        } else {
            final Class<?> testClass = extensionContext.getRequiredTestClass();
            if (testClass.isAnnotationPresent(MicronautTest.class)) {
                return ConditionEvaluationResult.enabled("Test bean active");
            } else {
                return ConditionEvaluationResult.disabled("Not a Micronaut test");
            }
        }
    }

    private void alignMocks(Object instance) {
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
}