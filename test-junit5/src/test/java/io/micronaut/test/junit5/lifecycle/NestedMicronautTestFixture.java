package io.micronaut.test.junit5.lifecycle;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * A nested class cannot have its own context, so its own {@code @MicronautTest} must be rejected.
 */
@MicronautTest
@Tag(LifecycleFixtures.TAG)
class NestedMicronautTestFixture {

    @Inject
    Counter counter;

    @Test
    void outer() {
        assertEquals(1, counter.value());
    }

    @Nested
    @MicronautTest
    class Inner {

        @Test
        void inner() {
            fail("the nested class should have been rejected before any of its tests ran");
        }
    }
}
