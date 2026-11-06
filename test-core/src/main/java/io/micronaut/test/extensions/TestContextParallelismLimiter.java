/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.test.extensions;

import io.micronaut.core.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

final class TestContextParallelismLimiter {

    static final String CONTEXT_PARALLELISM_PROPERTY = AbstractMicronautExtension.CONTEXT_PARALLELISM_PROPERTY;
    static final String TIMEOUT_PROPERTY = CONTEXT_PARALLELISM_PROPERTY + ".timeout";

    private static final Logger LOG = LoggerFactory.getLogger(TestContextParallelismLimiter.class);
    private static final long WARN_AFTER_SECONDS = 60;
    private static final String DEFAULT_TIMEOUT_SECONDS = "600";
    private static final ConcurrentMap<Integer, Semaphore> SEMAPHORES = new ConcurrentHashMap<>();

    private TestContextParallelismLimiter() {
    }

    @Nullable
    static Lease acquire() {
        String value = System.getProperty(CONTEXT_PARALLELISM_PROPERTY);
        if (value == null) {
            return null;
        }
        int maxActiveContexts = parsePositive(CONTEXT_PARALLELISM_PROPERTY, value);
        long timeoutSeconds = parsePositive(TIMEOUT_PROPERTY, System.getProperty(TIMEOUT_PROPERTY, DEFAULT_TIMEOUT_SECONDS));
        Semaphore semaphore = SEMAPHORES.computeIfAbsent(maxActiveContexts, permits -> new Semaphore(permits, true));
        try {
            if (!semaphore.tryAcquire(0, TimeUnit.SECONDS)) {
                acquireWithWarning(semaphore, maxActiveContexts, timeoutSeconds);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for a Micronaut test context permit", e);
        }
        return semaphore::release;
    }

    private static void acquireWithWarning(Semaphore semaphore, int maxActiveContexts, long timeoutSeconds) throws InterruptedException {
        // warns from a timer, since leaving a timed wait would give up this waiter's place in the fair queue
        Executor afterDelay = CompletableFuture.delayedExecutor(WARN_AFTER_SECONDS, TimeUnit.SECONDS);
        CompletableFuture<Void> warning = CompletableFuture.runAsync(() ->
            LOG.warn("Waited {} seconds for a test context permit with {}={}. If this never ends, another test class may be holding a permit while it waits for a resource this class has locked, such as with a class-level @ResourceLock.",
                WARN_AFTER_SECONDS, CONTEXT_PARALLELISM_PROPERTY, maxActiveContexts), afterDelay);
        try {
            if (!semaphore.tryAcquire(timeoutSeconds, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Gave up after " + timeoutSeconds + " seconds waiting for a test context permit with " + CONTEXT_PARALLELISM_PROPERTY + "=" + maxActiveContexts + ". Another test class may be holding a permit while it waits for a resource this class has locked, such as with a class-level @ResourceLock. If the classes are only slow, raise " + TIMEOUT_PROPERTY + ".");
            }
        } finally {
            warning.cancel(false);
        }
    }

    private static int parsePositive(String property, String value) {
        int parsed;
        try {
            parsed = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            parsed = 0;
        }
        if (parsed < 1) {
            throw new IllegalArgumentException("System property '" + property + "' must be a number greater than zero, but was '" + value + "'");
        }
        return parsed;
    }

    @FunctionalInterface
    interface Lease {
        void release();
    }
}
