package io.micronaut.test.junit5.lifecycle;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The context rebuilt for the first test fails to start, which must not affect the second test.
 */
@MicronautTest(rebuildContext = true)
@Tag(LifecycleFixtures.TAG)
@Property(name = "spec.name", value = RebuildContextFailedStartFixture.SPEC)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RebuildContextFailedStartFixture {

    static final String SPEC = "RebuildContextFailedStartFixture";

    @Inject
    ApplicationContext context;

    @Test
    @Order(1)
    @Property(name = "rebuild.method-property", value = "true")
    void contextFailsToStart() {
    }

    @Test
    @Order(2)
    void nextTestGetsARunningContext() {
        assertTrue(context.isRunning());
    }

    @Context
    @Requires(property = "spec.name", value = SPEC)
    static class FailsOnSecondStart {
        static final AtomicInteger STARTS = new AtomicInteger();

        FailsOnSecondStart() {
            if (STARTS.incrementAndGet() == 2) {
                throw new IllegalStateException("transient startup failure");
            }
        }
    }
}
