package io.micronaut.test.junit5;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

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
