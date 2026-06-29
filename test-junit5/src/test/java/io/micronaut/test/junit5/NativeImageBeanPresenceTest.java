package io.micronaut.test.junit5;

import io.micronaut.test.annotation.MicronautTestValue;
import io.micronaut.test.extensions.AbstractMicronautExtension;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeImageBeanPresenceTest {
    private static final String NATIVE_IMAGE_CODE_PROPERTY = "org.graalvm.nativeimage.imagecode";

    private final TestExtension extension = new TestExtension();

    @Test
    void testBeanPresenceStillRequiresGeneratedDefinitionOnJvm() {
        assertFalse(extension.hasBeanDefinition(PlainClass.class));
    }

    @Test
    void testBeanPresenceIsBypassedInNativeImage() {
        String previous = System.getProperty(NATIVE_IMAGE_CODE_PROPERTY);
        System.setProperty(NATIVE_IMAGE_CODE_PROPERTY, "runtime");
        try {
            assertTrue(extension.hasBeanDefinition(PlainClass.class));
        } finally {
            restoreNativeImageProperty(previous);
        }
    }

    private static void restoreNativeImageProperty(String previous) {
        if (previous == null) {
            System.clearProperty(NATIVE_IMAGE_CODE_PROPERTY);
        } else {
            System.setProperty(NATIVE_IMAGE_CODE_PROPERTY, previous);
        }
    }

    static final class PlainClass {
    }

    static final class TestExtension extends AbstractMicronautExtension<Object> {
        boolean hasBeanDefinition(Class<?> testClass) {
            return isTestSuiteBeanPresent(testClass);
        }

        @Override
        protected void resolveTestProperties(Object context, MicronautTestValue testAnnotationValue, Map<String, Object> testProperties) {
        }

        @Override
        protected void alignMocks(Object context, Object instance) {
        }
    }
}
