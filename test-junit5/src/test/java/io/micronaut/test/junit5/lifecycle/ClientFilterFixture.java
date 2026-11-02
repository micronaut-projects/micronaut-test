package io.micronaut.test.junit5.lifecycle;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.ClientFilter;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A test class that is itself a {@code @ClientFilter}, which used to recurse until the stack
 * overflowed.
 */
@MicronautTest
@ClientFilter("/client-filter-fixture/**")
@Tag(LifecycleFixtures.TAG)
@Property(name = "spec.name", value = "ClientFilterFixture")
class ClientFilterFixture {

    @Inject
    @Client("/")
    HttpClient client;

    @RequestFilter
    void addHeader(MutableHttpRequest<?> request) {
        request.header("X-Test", "from-test-class");
    }

    @Test
    void clientIsInjected() {
        assertNotNull(client);
    }
}
