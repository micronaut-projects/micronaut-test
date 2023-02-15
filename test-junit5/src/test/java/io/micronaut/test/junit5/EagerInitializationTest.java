package io.micronaut.test.junit5;

import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.ApplicationContextConfigurer;
import io.micronaut.context.annotation.ContextConfigurer;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.SupplierUtil;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.HttpClient;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

@MicronautTest
@Property(name = "spec.name", value = "EagerSingletonTest")
class EagerSingletonTest {

    // tag::eager[]
    @Inject
    EmbeddedServer server; // <1>

    Supplier<HttpClient> client = SupplierUtil.memoizedNonEmpty(() ->
        server.getApplicationContext().createBean(HttpClient.class, server.getURL())); // <2>

    @Test
    void testEagerSingleton() {
        Assertions.assertEquals("eager", client.get().toBlocking().retrieve("/eager")); // <3>
    }
    // end::eager[]

    @Requires(property = "spec.name", value = "EagerSingletonTest")
    @ContextConfigurer
    public static class Configurer implements ApplicationContextConfigurer {
        @Override
        public void configure(@NonNull ApplicationContextBuilder builder) {
            builder.eagerInitSingletons(true);
        }
    }

    @Requires(property = "spec.name", value = "EagerSingletonTest")
    @Controller("/eager")
    public static class EagerController {
        @Get
        String test() {
            return "eager";
        }
    }
}
