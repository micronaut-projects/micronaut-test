package io.micronaut.test.junit5.parallel;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A {@code @Nested} class reuses the extension instance of its outermost enclosing class, so its
 * methods must be serialised against the outer class's methods too - not just against each other.
 */
@MicronautTest
@Tag(ParallelFixtures.TAG)
class NestedClassFixture {

    static final String KEY = "nested-class";

    @Test
    void outerOne() {
        assertEquals(1, ConcurrencyRecorder.hold(KEY, 50), ParallelFixtures.NO_OVERLAP);
    }

    @Test
    void outerTwo() {
        assertEquals(1, ConcurrencyRecorder.hold(KEY, 50), ParallelFixtures.NO_OVERLAP);
    }

    @Nested
    class Inner {

        @Test
        void innerOne() {
            assertEquals(1, ConcurrencyRecorder.hold(KEY, 50), ParallelFixtures.NO_OVERLAP);
        }

        @Test
        void innerTwo() {
            assertEquals(1, ConcurrencyRecorder.hold(KEY, 50), ParallelFixtures.NO_OVERLAP);
        }

        @Nested
        class Innermost {

            @Test
            void innermost() {
                assertEquals(1, ConcurrencyRecorder.hold(KEY, 50), ParallelFixtures.NO_OVERLAP);
            }
        }
    }
}
