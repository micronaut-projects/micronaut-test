/*
 * Copyright 2017-2025 original authors
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
package io.micronaut.test.leak;

import io.micronaut.core.annotation.Internal;
import io.netty.util.LeakPresenceDetector;

import java.util.concurrent.TimeUnit;

/**
 * LeakPresenceDetector that uses an InheritableThreadLocal to transfer the resource scope from
 * test methods to event loops created within those threads.
 */
@Internal
public final class WithTransferableScope<T> extends LeakPresenceDetector<T> {
    static final InheritableThreadLocal<ResourceScope> SCOPE = new InheritableThreadLocal<>();

    @SuppressWarnings("unused")
    public WithTransferableScope(Class<?> resourceType, int samplingInterval) {
        super(resourceType);
    }

    @SuppressWarnings("unused")
    public WithTransferableScope(Class<?> resourceType, int samplingInterval, long maxActive) {
        super(resourceType);
    }

    static void init() {
        System.setProperty("io.netty.customResourceLeakDetector", WithTransferableScope.class.getName());
    }

    static void closeWithGracePeriod(ResourceScope scope) throws InterruptedException {
        // Wait some time for resources to close. Many tests do loop.shutdownGracefully without waiting, and that's ok.
        long start = System.nanoTime();
        while (scope.hasOpenResources() && System.nanoTime() - start < TimeUnit.SECONDS.toNanos(5)) {
            TimeUnit.MILLISECONDS.sleep(100);
        }

        scope.close();
    }

    @Override
    protected ResourceScope currentScope() {
        ResourceScope scope = SCOPE.get();
        if (scope == null) {
            // OOM is treated specially by netty
            throw new OutOfMemoryError("Resource created outside test?");
        }
        return scope;
    }
}
