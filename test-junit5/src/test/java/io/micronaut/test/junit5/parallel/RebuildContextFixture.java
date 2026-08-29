package io.micronaut.test.junit5.parallel;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@code rebuildContext = true} stops and replaces the application context in {@code beforeEach}. A
 * concurrent method would either be injected from the context that is about to be destroyed or find
 * its own context stopped mid-test.
 */
@MicronautTest(rebuildContext = true)
@Tag(ParallelFixtures.TAG)
class RebuildContextFixture {

    @Inject
    ApplicationContext applicationContext;

    @Inject
    ContextIdentity injectedIdentity;

    @Test
    void one() {
        assertOwnContext();
    }

    @Test
    void two() {
        assertOwnContext();
    }

    @Test
    void three() {
        assertOwnContext();
    }

    @Test
    void four() {
        assertOwnContext();
    }

    private void assertOwnContext() {
        assertTrue(applicationContext.isRunning(), "context was stopped while the test was running");
        assertSame(applicationContext.getBean(ContextIdentity.class), injectedIdentity,
            "injected bean came from a different application context");
        assertEquals(1, ConcurrencyRecorder.hold("rebuild-context", 30), ParallelFixtures.NO_OVERLAP);
        assertTrue(applicationContext.isRunning(), "context was stopped while the test was running");
        assertSame(applicationContext.getBean(ContextIdentity.class), injectedIdentity,
            "the application context was rebuilt while the test was running");
    }
}
