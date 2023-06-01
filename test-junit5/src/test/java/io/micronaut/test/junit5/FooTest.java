package io.micronaut.test.junit5;

import java.util.stream.Stream;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@MicronautTest(resolveParameters = false)
class FooTest {

    static Stream<Arguments> fooArgs() {
        return Stream.of(Arguments.of(new Foo()));
    }

    @ParameterizedTest
    @MethodSource("fooArgs")
    void foo(Foo arg) {
        Assertions.assertNotNull(arg);
    }

    @Singleton
    static class Foo {
    }
}

