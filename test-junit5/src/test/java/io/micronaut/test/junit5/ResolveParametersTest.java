package io.micronaut.test.junit5;

import java.util.stream.Stream;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@MicronautTest(resolveParameters = false)
class ResolveParametersTest {
    private static final Foo FOO_INSTANCE = new Foo();
    static Stream<Arguments> fooArgs() {
        return Stream.of(Arguments.of(FOO_INSTANCE));
    }

    private final Foo foo;

    ResolveParametersTest(Foo foo) {
        this.foo = foo;
    }

    @ParameterizedTest
    @MethodSource("fooArgs")
    void testMethodInjection(Foo arg) {
        Assertions.assertNotNull(arg);
        Assertions.assertSame(arg, FOO_INSTANCE);
    }

    @Test
    void testConstructorInjection() {
        Assertions.assertNotNull(foo);
    }

    @Singleton
    static class Foo {
    }
}

