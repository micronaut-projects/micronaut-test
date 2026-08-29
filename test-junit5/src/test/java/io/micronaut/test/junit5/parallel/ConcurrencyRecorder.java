package io.micronaut.test.junit5.parallel;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Records how many test executions sharing a key were in flight at the same time.
 *
 * <p>Fixtures call {@link #record} from their test methods; the driver test then asserts on the
 * observed maximum. A maximum of {@code 1} proves the executions were serialised.</p>
 */
final class ConcurrencyRecorder {

    private static final ConcurrentMap<String, Counters> COUNTERS = new ConcurrentHashMap<>();

    private ConcurrencyRecorder() {
    }

    static void reset(String key) {
        COUNTERS.remove(key);
    }

    /**
     * Marks the calling thread as active for {@code key} and keeps it there for {@code holdMillis},
     * so that anything else entering the same key in that window is observed.
     *
     * <p>The thread is parked rather than slept: this is not waiting for something to happen, it is
     * deliberately occupying the window in which an overlap would show up.</p>
     *
     * @param key        the key being guarded
     * @param holdMillis how long to occupy the window
     * @return how many executions were inside {@code key} when this one entered, itself included -
     *     1 when the executions are properly serialised
     */
    static int hold(String key, long holdMillis) {
        Counters counters = COUNTERS.computeIfAbsent(key, k -> new Counters());
        counters.invocations.incrementAndGet();
        counters.threads.add(Thread.currentThread().getName());
        int active = counters.active.incrementAndGet();
        counters.max.accumulateAndGet(active, Math::max);
        try {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(holdMillis));
            return Math.max(active, counters.active.get());
        } finally {
            counters.active.decrementAndGet();
        }
    }

    static int maxConcurrency(String key) {
        Counters counters = COUNTERS.get(key);
        return counters == null ? 0 : counters.max.get();
    }

    static int invocations(String key) {
        Counters counters = COUNTERS.get(key);
        return counters == null ? 0 : counters.invocations.get();
    }

    static int distinctThreads(String key) {
        Counters counters = COUNTERS.get(key);
        return counters == null ? 0 : counters.threads.size();
    }

    private static final class Counters {
        private final AtomicInteger active = new AtomicInteger();
        private final AtomicInteger max = new AtomicInteger();
        private final AtomicInteger invocations = new AtomicInteger();
        private final Set<String> threads = ConcurrentHashMap.newKeySet();
    }
}
