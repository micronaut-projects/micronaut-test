package io.micronaut.test.junit5;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
class ApplicationStopTest {

    @Inject
    private ApplicationContext applicationContext;

    @Test
    void stoppingTheContextDoesntCauseFailures() {
        applicationContext.stop();
        assertTrue(true);
        // should not error
    }
}
