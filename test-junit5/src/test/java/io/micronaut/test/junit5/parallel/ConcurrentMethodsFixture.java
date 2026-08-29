package io.micronaut.test.junit5.parallel;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A class that declares {@code @Execution(CONCURRENT)} itself opts out of the per-context lock and
 * takes responsibility for its own thread safety - nothing here touches shared context state.
 */
@MicronautTest
@Execution(ExecutionMode.CONCURRENT)
@Tag(ParallelFixtures.TAG)
class ConcurrentMethodsFixture {

    static final String KEY = "concurrent-methods";

    @Test
    void one() {
        assertTrue(Rendezvous.meet(KEY), ParallelFixtures.MUST_MEET);
    }

    @Test
    void two() {
        assertTrue(Rendezvous.meet(KEY), ParallelFixtures.MUST_MEET);
    }
}
