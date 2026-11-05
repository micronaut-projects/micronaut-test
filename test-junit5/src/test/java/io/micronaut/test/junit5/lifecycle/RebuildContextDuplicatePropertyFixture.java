package io.micronaut.test.junit5.lifecycle;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The second test is rejected for duplicate method properties, which must not affect the third.
 */
@MicronautTest(rebuildContext = true)
@Tag(LifecycleFixtures.TAG)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RebuildContextDuplicatePropertyFixture {

    @Inject
    ApplicationContext context;

    @Test
    @Order(1)
    void firstTest() {
        assertTrue(context.isRunning());
    }

    @Test
    @Order(2)
    @Property(name = "rebuild.duplicate", value = "1")
    @Property(name = "rebuild.duplicate", value = "2")
    void duplicateProperties() {
    }

    @Test
    @Order(3)
    void nextTest() {
        assertTrue(context.isRunning());
    }
}
