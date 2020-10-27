
package io.micronaut.test.junit5;

import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.inject.Inject;

import static org.junit.Assert.assertFalse;

@MicronautTest(startApplication = false, rebuildContext = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DisableEmbeddedApplicationTest {

    @Inject
    private EmbeddedApplication<?> embeddedApplication;

    @Test
    @Order(1)
    void embeddedApplicationIsNotStartedWhenContextIsStarted() {
        assertFalse(embeddedApplication.isRunning());
    }

    @Test
    @Order(2)
    void embeddedApplicationIsNotStartedWhenContextIsRebuilt() {
        assertFalse(embeddedApplication.isRunning());
    }
}
