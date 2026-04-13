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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

final class TestContextParallelismLimiter {

    static final String CONTEXT_PARALLELISM_PROPERTY = "micronaut.test.context.parallelism";

    private static final ConcurrentMap<Integer, Semaphore> SEMAPHORES = new ConcurrentHashMap<>();

    private TestContextParallelismLimiter() {
    }

    @Nullable
    static Lease acquire() {
        Integer maxActiveContexts = Integer.getInteger(CONTEXT_PARALLELISM_PROPERTY);
        if (maxActiveContexts == null) {
            return null;
        }
        if (maxActiveContexts < 1) {
            throw new IllegalArgumentException("System property '" + CONTEXT_PARALLELISM_PROPERTY + "' must be greater than zero");
        }
        Semaphore semaphore = SEMAPHORES.computeIfAbsent(maxActiveContexts, permits -> new Semaphore(permits, true));
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for a Micronaut test context permit", e);
        }
        return semaphore::release;
    }

    @FunctionalInterface
    interface Lease {
        void release();
    }
}
