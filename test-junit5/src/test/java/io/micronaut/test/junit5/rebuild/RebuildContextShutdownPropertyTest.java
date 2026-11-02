package io.micronaut.test.junit5.rebuild;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import jakarta.inject.Inject;
import io.micronaut.context.event.ShutdownEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Under rebuildContext = true a method-level @Property stays in force until that test's context
 * has shut down.
 */
@MicronautTest(rebuildContext = true)
@Property(name = "spec.name", value = "RebuildContextShutdownPropertyTest")
@Property(name = "shutdown.value", value = "class")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RebuildContextShutdownPropertyTest {

    @Test
    @Order(1)
    @Property(name = "shutdown.value", value = "method")
    void overridesTheProperty() {
    }

    @Test
    @Order(2)
    void shutdownOfThePreviousContextSawTheOverride() {
        // the most recent shutdown is the first test's context, stopped to rebuild for this test
        assertEquals("method", ShutdownRecorder.SEEN.get(ShutdownRecorder.SEEN.size() - 1));
    }

    @Singleton
    @Requires(property = "spec.name", value = "RebuildContextShutdownPropertyTest")
    static class ShutdownRecorder {
        static final List<String> SEEN = new CopyOnWriteArrayList<>();

        @Inject
        Environment environment;

        @EventListener
        void onShutdown(ShutdownEvent event) {
            SEEN.add(environment.getProperty("shutdown.value", String.class).orElse(null));
        }
    }
}
