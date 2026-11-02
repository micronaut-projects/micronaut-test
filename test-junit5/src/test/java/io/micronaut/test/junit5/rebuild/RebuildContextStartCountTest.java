package io.micronaut.test.junit5.rebuild;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * With rebuildContext = true each test gets one fresh context: the context started for the class
 * serves the first test instead of being thrown away unused.
 */
@MicronautTest(rebuildContext = true)
@Property(name = "spec.name", value = "RebuildContextStartCountTest")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RebuildContextStartCountTest {

    @Test
    @Order(1)
    void firstTestUsesTheFirstContext() {
        assertEquals(1, EagerBean.CREATED.get());
    }

    @Test
    @Order(2)
    void secondTestGetsASecondContext() {
        assertEquals(2, EagerBean.CREATED.get());
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
