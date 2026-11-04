package io.micronaut.test.junit5.rebuild;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The first test uses the context started for the class, which is built before @BeforeAll methods
 * run. A context built for a test, as one with method-level properties gets, is built after them.
 */
@MicronautTest(rebuildContext = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RebuildContextBeforeAllTest {

    private static final String PUBLISHED_PROPERTY = "rebuild.before-all.url";

    @Inject
    ApplicationContext context;

    @BeforeAll
    static void publishProperty() {
        System.setProperty(PUBLISHED_PROPERTY, "http://localhost:1234");
    }

    @AfterAll
    static void clearProperty() {
        System.clearProperty(PUBLISHED_PROPERTY);
    }

    @Test
    @Order(1)
    void theClassContextPredatesBeforeAll() {
        assertEquals(Optional.empty(), context.getProperty(PUBLISHED_PROPERTY, String.class));
    }

    @Test
    @Order(2)
    @Property(name = "rebuild.method-property", value = "true")
    void aContextBuiltForATestSeesBeforeAll() {
        assertEquals(Optional.of("http://localhost:1234"), context.getProperty(PUBLISHED_PROPERTY, String.class));
    }
}
