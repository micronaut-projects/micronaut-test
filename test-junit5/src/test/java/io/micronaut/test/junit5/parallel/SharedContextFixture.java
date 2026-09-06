package io.micronaut.test.junit5.parallel;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Every method of one {@code @MicronautTest} class shares a single extension instance and a single
 * application context, so none of them may run at the same time.
 */
@MicronautTest
@Tag(ParallelFixtures.TAG)
class SharedContextFixture {

    static final String KEY = "shared-context";

    @Test
    void one() {
        assertEquals(1, ConcurrencyRecorder.hold(KEY, 50), ParallelFixtures.NO_OVERLAP);
    }

    @Test
    void two() {
        assertEquals(1, ConcurrencyRecorder.hold(KEY, 50), ParallelFixtures.NO_OVERLAP);
    }

    @Test
    void three() {
        assertEquals(1, ConcurrencyRecorder.hold(KEY, 50), ParallelFixtures.NO_OVERLAP);
    }

    @Test
    void four() {
        assertEquals(1, ConcurrencyRecorder.hold(KEY, 50), ParallelFixtures.NO_OVERLAP);
    }

    @Test
    void five() {
        assertEquals(1, ConcurrencyRecorder.hold(KEY, 50), ParallelFixtures.NO_OVERLAP);
    }

    @Test
    void six() {
        assertEquals(1, ConcurrencyRecorder.hold(KEY, 50), ParallelFixtures.NO_OVERLAP);
    }
}
