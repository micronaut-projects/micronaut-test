package io.micronaut.test.junit5.server;

import io.micronaut.context.ApplicationContext;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.micronaut.test.support.server.TestEmbeddedServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import jakarta.inject.Inject;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExternalServerTest implements TestPropertyProvider {

    static EmbeddedServer EXTERNAL_SERVER = ApplicationContext.run(
            EmbeddedServer.class,
                Collections.singletonMap("micronaut.server.port", -1)
            );
    static final int PORT = EXTERNAL_SERVER.getPort();

    @Inject
    @Client("/")
    RxHttpClient client;

    @Inject
    EmbeddedServer embeddedServer;

    @AfterAll
    static void shutdown() {
        EXTERNAL_SERVER.stop();
    }

    @Test
    void testServerAvailable() {
        HttpResponse<String> response = client.exchange("/math/compute/10", String.class).blockingFirst();

        assertTrue(
            embeddedServer instanceof TestEmbeddedServer
        );
        assertTrue(
                embeddedServer.isRunning()
        );
        assertEquals(
                embeddedServer.getPort(),
                PORT
        );
        assertEquals(
                HttpStatus.OK,
                response.status()
        );
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.singletonMap(
                TestEmbeddedServer.PROPERTY, EXTERNAL_SERVER.getURL().toString()
        );
    }
}
