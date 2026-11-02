package io.micronaut.test.junit5;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A property source that cannot be found is skipped with a warning; the context still starts.
 */
@MicronautTest(propertySources = "does-not-exist.properties")
class MissingPropertySourceTest {

    @Inject
    ApplicationContext applicationContext;

    @Test
    void theContextStartsWithoutIt() {
        assertTrue(applicationContext.isRunning());
    }
}
