package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@Property(name = "foo.bar", value = "stuff")
@TestMethodOrder(OrderAnnotation.class)
class PropertyValueTest {

    @Property(name = "foo.bar")
    String val;

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
}
