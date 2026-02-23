package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Assertions;

@Property(name = "spec.name", value = "ResolveParametersTest")
@MicronautTest
class ResolveParametersTest {

    @Test
    void bar(Foo arg) {
        Assertions.assertNotNull(arg);
    }

    @Requires(property = "spec.name", value = "ResolveParametersTest")
    @Singleton
    static class Foo {
    }
}

