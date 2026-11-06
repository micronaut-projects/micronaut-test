package io.micronaut.test.junit5.lifecycle;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.ClientFilter;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * A {@code @ClientFilter} test class next to an eager bean that creates the filter while the
 * context starts, so the check has to come before that.
 */
@MicronautTest
@ClientFilter("/client-filter-eager-fixture/**")
@Tag(LifecycleFixtures.TAG)
@Property(name = "spec.name", value = ClientFilterEagerBeanFixture.SPEC)
class ClientFilterEagerBeanFixture {

    static final String SPEC = "ClientFilterEagerBeanFixture";

    @RequestFilter
    void addHeader(MutableHttpRequest<?> request) {
        request.header("X-Test", "from-test-class");
    }

    @Test
    void neverRuns() {
    }

    @Context
    @Requires(property = "spec.name", value = SPEC)
    static class EagerClientUser {
        EagerClientUser(@Client("/") HttpClient client) {
        }
    }
}
