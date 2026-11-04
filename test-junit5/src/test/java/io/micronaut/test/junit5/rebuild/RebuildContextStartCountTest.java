package io.micronaut.test.junit5.rebuild;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * With rebuildContext = true each test gets one fresh context: the context started for the class
 * serves the first test instead of being thrown away unused.
 */
@MicronautTest(rebuildContext = true)
@Property(name = "spec.name", value = "RebuildContextStartCountTest")
class RebuildContextStartCountTest {

    private static final AtomicInteger TESTS_RUN = new AtomicInteger();

    @AfterAll
    static void resetCounters() {
        TESTS_RUN.set(0);
        EagerBean.CREATED.set(0);
    }

    @Test
    void aTestGetsOneContext() {
        assertEquals(TESTS_RUN.incrementAndGet(), EagerBean.CREATED.get());
    }

    @Test
    void anotherTestGetsOneContext() {
        assertEquals(TESTS_RUN.incrementAndGet(), EagerBean.CREATED.get());
    }

    @Context
    @Requires(property = "spec.name", value = "RebuildContextStartCountTest")
    static class EagerBean {
        static final AtomicInteger CREATED = new AtomicInteger();

        EagerBean() {
            CREATED.incrementAndGet();
        }
    }
}
