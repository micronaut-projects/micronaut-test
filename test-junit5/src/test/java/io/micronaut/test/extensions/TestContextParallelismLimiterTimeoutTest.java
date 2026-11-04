package io.micronaut.test.extensions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A class waiting for a permit gives up with an explanation rather than waiting forever, as it
 * would when deadlocked with the class holding the permit.
 */
@Isolated("toggles global system properties")
class TestContextParallelismLimiterTimeoutTest {

    @AfterEach
    void clearProperties() {
        System.clearProperty(TestContextParallelismLimiter.CONTEXT_PARALLELISM_PROPERTY);
        System.clearProperty(TestContextParallelismLimiter.TIMEOUT_PROPERTY);
    }

    @Test
    void waitingForAPermitTimesOut() {
        // a limit no other test uses, since permits are shared per limit across the JVM
        System.setProperty(TestContextParallelismLimiter.CONTEXT_PARALLELISM_PROPERTY, "2");
        System.setProperty(TestContextParallelismLimiter.TIMEOUT_PROPERTY, "1");
        TestContextParallelismLimiter.Lease first = TestContextParallelismLimiter.acquire();
        TestContextParallelismLimiter.Lease second = TestContextParallelismLimiter.acquire();
        try {
            IllegalStateException failure = assertThrows(IllegalStateException.class, TestContextParallelismLimiter::acquire);

            assertTrue(failure.getMessage().contains(TestContextParallelismLimiter.TIMEOUT_PROPERTY), failure.getMessage());
            assertTrue(failure.getMessage().contains("@ResourceLock"), failure.getMessage());
        } finally {
            first.release();
            second.release();
        }
        TestContextParallelismLimiter.Lease afterRelease = TestContextParallelismLimiter.acquire();
        assertNotNull(afterRelease);
        afterRelease.release();
    }

    @Test
    void anInvalidTimeoutIsRejected() {
        System.setProperty(TestContextParallelismLimiter.CONTEXT_PARALLELISM_PROPERTY, "2");
        System.setProperty(TestContextParallelismLimiter.TIMEOUT_PROPERTY, "soon");

        assertThrows(IllegalArgumentException.class, TestContextParallelismLimiter::acquire);
    }
}
