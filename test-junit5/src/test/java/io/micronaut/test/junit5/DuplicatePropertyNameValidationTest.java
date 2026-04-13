package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DuplicatePropertyNameValidationTest {

    @Test
    void rejectsDuplicateClassLevelProperties() {
        TestMicronautJunit5Extension extension = new TestMicronautJunit5Extension();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> extension.start(DuplicateClassLevelPropertyTest.class));

        assertEquals(
            "Duplicate @Property name [test.property] declared on test class io.micronaut.test.junit5.DuplicateClassLevelPropertyTest",
            exception.getMessage()
        );
    }

    @Test
    void rejectsDuplicateMethodLevelProperties() {
        TestMicronautJunit5Extension extension = new TestMicronautJunit5Extension();
        extension.start(UniqueClassLevelPropertyTest.class);

        try {
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> extension.beforeEach("duplicateMethodLevelProperty"));

            assertEquals(
                "Duplicate @Property name [test.property] declared on test method void io.micronaut.test.junit5.UniqueClassLevelPropertyTest.duplicateMethodLevelProperty()",
                exception.getMessage()
            );
        } finally {
            extension.stop();
        }
    }

    @Test
    void allowsMethodLevelOverrideOfClassLevelProperty() {
        TestMicronautJunit5Extension extension = new TestMicronautJunit5Extension();
        extension.start(UniqueClassLevelPropertyTest.class);

        try {
            assertDoesNotThrow(() -> extension.beforeEach("overrideClassLevelProperty"));
        } finally {
            extension.stop();
        }
    }

    static final class TestMicronautJunit5Extension extends MicronautJunit5Extension {
        void start(Class<?> testClass) {
            beforeClass(null, testClass, buildMicronautTestValue(testClass));
        }

        void beforeEach(String methodName) throws NoSuchMethodException {
            Method method = UniqueClassLevelPropertyTest.class.getDeclaredMethod(methodName);
            beforeEach(null, null, method, Arrays.asList(method.getAnnotationsByType(Property.class)));
        }

        void stop() {
            afterClass(null);
        }
    }
}

@MicronautTest
@Property(name = "test.property", value = "first")
@Property(name = "test.property", value = "second")
class DuplicateClassLevelPropertyTest {
}

@MicronautTest
@Property(name = "test.property", value = "class-level")
class UniqueClassLevelPropertyTest {

    @Property(name = "test.property", value = "first")
    @Property(name = "test.property", value = "second")
    void duplicateMethodLevelProperty() {
    }

    @Property(name = "test.property", value = "method-level")
    void overrideClassLevelProperty() {
    }
}
