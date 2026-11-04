package io.micronaut.test.junit5.lifecycle;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * A nested class cannot have its own context, so {@code @MicronautTest} through a composed
 * annotation must be rejected like a direct one.
 */
@MicronautTest
@Tag(LifecycleFixtures.TAG)
class NestedComposedMicronautTestFixture {

    @Test
    void outer() {
    }

    @Nested
    @ComposedMicronautTest
    class Inner {

        @Test
        void inner() {
            fail("the nested class should have been rejected before any of its tests ran");
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @MicronautTest(environments = "composed")
    @interface ComposedMicronautTest {
    }
}
