package io.micronaut.test.junit5.parallel;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A meeting point used to prove that executions really do overlap.
 *
 * <p>If the participants are serialised the first one to arrive times out and the test fails, so a
 * passing rendezvous is positive evidence of parallelism rather than of its absence.</p>
 */
final class Rendezvous {

    private static final ConcurrentMap<String, CyclicBarrier> BARRIERS = new ConcurrentHashMap<>();
    private static final long TIMEOUT_SECONDS = 30;

    private Rendezvous() {
    }

    static void expect(String key, int parties) {
        BARRIERS.put(key, new CyclicBarrier(parties));
    }

    /**
     * Waits for the other participants of {@code key} to arrive.
     *
     * @param key the rendezvous to join
     * @return true if every participant arrived, false if this one waited alone until the timeout -
     *     which is what a serialised execution looks like
     */
    static boolean meet(String key) {
        CyclicBarrier barrier = BARRIERS.get(key);
        if (barrier == null) {
            throw new IllegalStateException("No rendezvous registered for " + key);
        }
        try {
            barrier.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return true;
        } catch (TimeoutException | BrokenBarrierException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
