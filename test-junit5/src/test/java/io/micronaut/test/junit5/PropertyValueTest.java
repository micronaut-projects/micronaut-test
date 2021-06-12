
package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
@Property(name = "foo.bar", value = "stuff")
@TestMethodOrder(OrderAnnotation.class)
class PropertyValueTest {

    @Property(name = "foo.bar")
    String val;

    @Property(name = "prop.from.yml")
    String fromYml;

    @Test
    @Order(1)
    void testInitialValue() {
        assertEquals("stuff", val);
    }

    @Property(name = "foo.bar", value = "changed")
    @Test
    @Order(2)
    void testValueChanged() {
        assertEquals("changed", val);
    }

    @Test
    @Order(3)
    void testValueRestored() {
        assertEquals("stuff", val);
    }

    @Test
    @Property(name = "prop.from.yml", value = "local")
    @Order(4)
    void testValueOverridenFromConfig() {
        assertEquals("local", fromYml);
    }

    @Test
    @Order(5)
    void testValueFromConfig() {
        assertEquals("yml", fromYml);
    }
}
