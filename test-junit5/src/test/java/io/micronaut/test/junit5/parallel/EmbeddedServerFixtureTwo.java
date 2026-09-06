package io.micronaut.test.junit5.parallel;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @see EmbeddedServerFixtureOne
 */
@MicronautTest
@Tag(ParallelFixtures.TAG)
@Property(name = "spec.name", value = "EmbeddedServerFixture")
class EmbeddedServerFixtureTwo {

    @Inject
    EmbeddedServer embeddedServer;

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void servesItsOwnServerWhileTheOtherClassRuns() {
        assertTrue(embeddedServer.isRunning());
        assertTrue(Rendezvous.meet(EmbeddedServerFixtureOne.KEY), ParallelFixtures.MUST_MEET);
        assertEquals("pong", client.toBlocking().retrieve(HttpRequest.GET("/parallel/ping")));
    }
}
