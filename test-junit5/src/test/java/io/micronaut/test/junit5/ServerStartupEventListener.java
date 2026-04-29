package io.micronaut.test.junit5;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;

import java.util.concurrent.atomic.AtomicInteger;

@Singleton
class ServerStartupEventListener {
    private final AtomicInteger invocationCount = new AtomicInteger();

    @EventListener
    void onStartup(ServerStartupEvent event) {
        invocationCount.incrementAndGet();
    }

    int getInvocationCount() {
        return invocationCount.get();
    }
}
