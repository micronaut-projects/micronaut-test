package io.micronaut.test.junit5;

import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
class StartEmbeddedApplicationTest {

    @Inject
    private EmbeddedApplication<?> embeddedApplication;

    @Inject
    private ServerStartupEventListener serverStartupEventListener;

    @Test
    void embeddedApplicationPublishesServerStartupEvent() {
        assertTrue(embeddedApplication.isRunning());
        assertEquals(1, serverStartupEventListener.getInvocationCount());
    }
}
