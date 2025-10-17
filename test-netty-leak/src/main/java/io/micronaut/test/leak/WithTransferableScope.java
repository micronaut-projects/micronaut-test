package io.micronaut.test.leak;

import io.micronaut.core.annotation.Internal;
import io.netty.util.LeakPresenceDetector;

import java.util.concurrent.TimeUnit;

@Internal
public final class WithTransferableScope<T> extends LeakPresenceDetector<T> {
    static final InheritableThreadLocal<ResourceScope> SCOPE = new InheritableThreadLocal<>();

    static void init() {
        System.setProperty("io.netty.customResourceLeakDetector", WithTransferableScope.class.getName());
    }

    @SuppressWarnings("unused")
    public WithTransferableScope(Class<?> resourceType, int samplingInterval) {
        super(resourceType);
    }

    @SuppressWarnings("unused")
    public WithTransferableScope(Class<?> resourceType, int samplingInterval, long maxActive) {
        super(resourceType);
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
