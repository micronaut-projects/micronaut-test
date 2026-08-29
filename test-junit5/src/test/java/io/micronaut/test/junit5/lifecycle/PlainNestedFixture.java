package io.micronaut.test.junit5.lifecycle;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A nested class that declares no configuration of its own stays perfectly legal.
 */
@MicronautTest
@Tag(LifecycleFixtures.TAG)
class PlainNestedFixture {

    @Inject
    Counter counter;

    @Test
    void outer() {
        assertEquals(1, counter.value());
    }

    @Nested
    class Inner {

        @Test
        void inner() {
            assertEquals(1, counter.value());
        }
    }
}
