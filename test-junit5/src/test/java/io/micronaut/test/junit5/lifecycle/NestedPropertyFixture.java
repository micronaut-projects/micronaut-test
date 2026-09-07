package io.micronaut.test.junit5.lifecycle;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * A @Property on a nested class used to be ignored without a word.
 */
@MicronautTest
@Property(name = "lifecycle.scope", value = "outer")
@Tag(LifecycleFixtures.TAG)
class NestedPropertyFixture {

    @Inject
    Counter counter;

    @Test
    void outer() {
        assertEquals(1, counter.value());
    }

    @Nested
    @Property(name = "lifecycle.scope", value = "inner")
    class Inner {

        @Test
        void inner() {
            fail("the nested class should have been rejected before any of its tests ran");
        }
    }
}
