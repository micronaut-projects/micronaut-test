package io.micronaut.test.junit5.parallel;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import jakarta.annotation.PreDestroy;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Counts the application contexts of the {@code ContextParallelismFixture} classes that are
 * running at the same time.
 */
@Context
@Requires(property = "spec.name", value = ActiveContextProbe.SPEC)
class ActiveContextProbe {

    static final String SPEC = "ContextParallelism";
    static final String KEY = "context-parallelism";

    private static final AtomicInteger ACTIVE = new AtomicInteger();
    private static final AtomicInteger MAX_ACTIVE = new AtomicInteger();
    private static volatile boolean awaitOverlap;

    ActiveContextProbe() {
        MAX_ACTIVE.accumulateAndGet(ACTIVE.incrementAndGet(), Math::max);
        if (awaitOverlap) {
            // without a limit both contexts must be alive here at once
            Rendezvous.meet(KEY);
        } else {
            // keep this context alive long enough for an unlimited second context to start
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @PreDestroy
    void close() {
        ACTIVE.decrementAndGet();
    }

    static void reset(boolean expectOverlap) {
        ACTIVE.set(0);
        MAX_ACTIVE.set(0);
        awaitOverlap = expectOverlap;
        if (expectOverlap) {
            Rendezvous.expect(KEY, 2);
        }
    }

    static int maxActive() {
        return MAX_ACTIVE.get();
    }
}
