package io.micronaut.test.junit5.rebuild;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * An injected HTTP client keeps working in every test when the context is rebuilt between them.
 */
@MicronautTest(rebuildContext = true)
@Property(name = "spec.name", value = "RebuildContextHttpClientTest")
class RebuildContextHttpClientTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void firstRequest() {
        assertEquals("pong", client.toBlocking().retrieve("/rebuild-ping"));
    }

    @Test
    void secondRequest() {
        assertEquals("pong", client.toBlocking().retrieve("/rebuild-ping"));
    }

    @Test
    void thirdRequest() {
        assertEquals("pong", client.toBlocking().retrieve("/rebuild-ping"));
    }

    @Controller("/rebuild-ping")
    @Requires(property = "spec.name", value = "RebuildContextHttpClientTest")
    static class PingController {
        @Get
        String ping() {
            return "pong";
        }
    }
}
