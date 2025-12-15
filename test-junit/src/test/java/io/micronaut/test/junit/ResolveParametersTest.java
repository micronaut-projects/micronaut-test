package io.micronaut.test.junit;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit.annotation.MicronautTest;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

