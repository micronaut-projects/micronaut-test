package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@Property(name = "foo.bar", value = "stuff")
class PropertyValueTest {

    @Property(name = "foo.bar")
    String val;

    @Test
    void testInitialValue() {
        assertEquals("stuff", val);
    }

    @Property(name = "foo.bar", value = "changed")
    @Test
    void testValueChanged() {
        assertEquals("changed", val);
    }

    @Test
    void testValueRestored() {
        assertEquals("stuff", val);
    }
}
