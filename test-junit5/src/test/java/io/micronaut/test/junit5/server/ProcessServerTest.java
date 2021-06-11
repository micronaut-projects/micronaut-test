package io.micronaut.test.junit5.server;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.micronaut.test.support.server.TestExecutableEmbeddedServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import jakarta.inject.Inject;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// tag::executable[]
@Property(
    name = TestExecutableEmbeddedServer.PROPERTY,
    value = "src/test/apps/test-app.jar"
)
// end::executable[]
public class ProcessServerTest implements TestPropertyProvider {

    @Inject
    EmbeddedServer embeddedServer;


    @Inject
    @Client("/")
    RxHttpClient client;

    @Test
    void testServerAvailable() {
        HttpResponse<String> response = client.exchange("/test", String.class).blockingFirst();

        assertTrue(
                embeddedServer instanceof TestExecutableEmbeddedServer
        );
        assertTrue(
                embeddedServer.isRunning()
        );
        assertEquals(
                HttpStatus.OK,
                response.status()
        );
        assertEquals(
                "Result = good",
                response.body()
        );
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.singletonMap(
                "test.property", "good"
        );
    }
}
