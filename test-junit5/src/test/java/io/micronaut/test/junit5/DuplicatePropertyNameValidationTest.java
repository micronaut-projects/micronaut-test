package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DuplicatePropertyNameValidationTest {

    @Test
    void rejectsDuplicateClassLevelProperties() {
        TestMicronautJunit5Extension extension = new TestMicronautJunit5Extension();

        try {
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> extension.start(DuplicateClassLevelPropertyFixture.class));

            assertEquals(
                "Duplicate @Property name [test.property] declared on test class io.micronaut.test.junit5.DuplicateClassLevelPropertyFixture",
                exception.getMessage()
            );
        } finally {
            extension.stop();
        }
    }

    @Test
    void rejectsDuplicateMethodLevelProperties() {
        TestMicronautJunit5Extension extension = new TestMicronautJunit5Extension();
        extension.start(UniqueClassLevelPropertyFixture.class);

        try {
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> extension.beforeEach(
                    "public void io.micronaut.test.junit5.UniqueClassLevelPropertyFixture.duplicateMethodLevelProperty()",
                    DuplicateMethodLevelPropertySource.class
                ));

            assertEquals(
                "Duplicate @Property name [test.property] declared on test method public void io.micronaut.test.junit5.UniqueClassLevelPropertyFixture.duplicateMethodLevelProperty()",
                exception.getMessage()
            );
        } finally {
            extension.stop();
        }
    }

    @Test
    void allowsMethodLevelOverrideOfClassLevelProperty() {
        TestMicronautJunit5Extension extension = new TestMicronautJunit5Extension();
        extension.start(UniqueClassLevelPropertyFixture.class);

        try {
            assertDoesNotThrow(() -> extension.beforeEach(
                "public void io.micronaut.test.junit5.UniqueClassLevelPropertyFixture.overrideClassLevelProperty()",
                OverrideMethodLevelPropertySource.class
            ));
        } finally {
            extension.stop();
        }
    }

    static final class TestMicronautJunit5Extension extends MicronautJunit5Extension {
        void start(Class<?> testClass) {
            beforeClass(null, testClass, buildMicronautTestValue(testClass));
        }

        void beforeEach(String methodDescription, Class<?> propertySource) {
            beforeEach(
                null,
                null,
                namedMethod(methodDescription),
                Arrays.asList(propertySource.getAnnotationsByType(Property.class))
            );
        }

        void stop() {
            afterClass(null);
        }

        private AnnotatedElement namedMethod(String methodDescription) {
            return new AnnotatedElement() {
                @Override
                public <T extends java.lang.annotation.Annotation> T getAnnotation(Class<T> annotationClass) {
                    return null;
                }

                @Override
                public java.lang.annotation.Annotation[] getAnnotations() {
                    return new java.lang.annotation.Annotation[0];
                }

                @Override
                public java.lang.annotation.Annotation[] getDeclaredAnnotations() {
                    return new java.lang.annotation.Annotation[0];
                }

                @Override
                public String toString() {
                    return methodDescription;
                }
            };
        }
    }
}

@MicronautTest
@Property(name = "test.property", value = "first")
@Property(name = "test.property", value = "second")
class DuplicateClassLevelPropertyFixture {
}

@MicronautTest
@Property(name = "test.property", value = "class-level")
class UniqueClassLevelPropertyFixture {
}

@Property(name = "test.property", value = "first")
@Property(name = "test.property", value = "second")
class DuplicateMethodLevelPropertySource {
}

@Property(name = "test.property", value = "method-level")
class OverrideMethodLevelPropertySource {
}
