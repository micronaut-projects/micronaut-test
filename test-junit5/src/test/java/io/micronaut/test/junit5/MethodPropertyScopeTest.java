package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.context.scope.Refreshable;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A method-level @Property applies to its own test only; later tests see the default again.
 */
@MicronautTest
@Property(name = "spec.name", value = "MethodPropertyScopeTest")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MethodPropertyScopeTest {

    @Inject
    Foo foo;

    @Test
    @Order(1)
    void usesTheDefault() {
        assertEquals("a", foo.getValue());
    }

    @Test
    @Order(2)
    @Property(name = "scoped.foo", value = "b")
    void usesTheOverride() {
        assertEquals("b", foo.getValue());
    }

    @Test
    @Order(3)
    void usesTheDefaultAgain() {
        assertEquals("a", foo.getValue());
    }

    @Refreshable
    @Requires(property = "spec.name", value = "MethodPropertyScopeTest")
    static class Foo {
        @Value("${scoped.foo:a}")
        String value;

        String getValue() {
            return value;
        }
    }
}
