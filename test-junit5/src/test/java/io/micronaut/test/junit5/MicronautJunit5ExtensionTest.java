package io.micronaut.test.junit5;

import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MicronautJunit5ExtensionTest {
    private static final String NATIVE_IMAGE_CODE_PROPERTY = "org.graalvm.nativeimage.imagecode";

    @Test
    void testConditionUsesResolvedSpecDefinitionWhenBeanLookupFails() {
        TestMicronautJunit5Extension extension = new TestMicronautJunit5Extension();
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        ExtensionContext extensionContext = mock(ExtensionContext.class);
        BeanDefinition<?> beanDefinition = mock(BeanDefinition.class);

        extension.setApplicationContext(applicationContext);
        extension.setSpecDefinition(beanDefinition);

        when(extensionContext.getTestInstance()).thenReturn(Optional.of(new AnnotatedTest()));
        doReturn(AnnotatedTest.class).when(extensionContext).getRequiredTestClass();
        when(applicationContext.containsBean(AnnotatedTest.class)).thenReturn(false);

        ConditionEvaluationResult result = evaluateInNativeImage(() -> extension.evaluateExecutionCondition(extensionContext));

        assertFalse(result.isDisabled());
    }

    @Test
    void testConditionStillDisablesWhenNoActiveBeanOrDefinitionExists() {
        TestMicronautJunit5Extension extension = new TestMicronautJunit5Extension();
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        ExtensionContext extensionContext = mock(ExtensionContext.class);

        extension.setApplicationContext(applicationContext);
        extension.setSpecDefinition(null);

        when(extensionContext.getTestInstance()).thenReturn(Optional.of(new AnnotatedTest()));
        doReturn(AnnotatedTest.class).when(extensionContext).getRequiredTestClass();
        when(applicationContext.containsBean(AnnotatedTest.class)).thenReturn(false);

        ConditionEvaluationResult result = evaluateInNativeImage(() -> extension.evaluateExecutionCondition(extensionContext));

        assertTrue(result.isDisabled());
    }

    private static ConditionEvaluationResult evaluateInNativeImage(ResultSupplier<ConditionEvaluationResult> supplier) {
        String previous = System.getProperty(NATIVE_IMAGE_CODE_PROPERTY);
        System.setProperty(NATIVE_IMAGE_CODE_PROPERTY, "runtime");
        try {
            return supplier.get();
        } finally {
            if (previous == null) {
                System.clearProperty(NATIVE_IMAGE_CODE_PROPERTY);
            } else {
                System.setProperty(NATIVE_IMAGE_CODE_PROPERTY, previous);
            }
        }
    }

    @MicronautTest
    static final class AnnotatedTest {
    }

    static final class TestMicronautJunit5Extension extends MicronautJunit5Extension {
        void setApplicationContext(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        void setSpecDefinition(BeanDefinition<?> specDefinition) {
            this.specDefinition = specDefinition;
        }
    }

    @FunctionalInterface
    interface ResultSupplier<T> {
        T get();
    }
}
