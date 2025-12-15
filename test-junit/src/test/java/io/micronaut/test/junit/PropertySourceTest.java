
package io.micronaut.test.junit;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest(propertySources = "myprops.properties")
class PropertySourceTest {

    @Property(name = "foo.bar")
    String val;


    @Test
    void testPropertySource() {
        Assertions.assertEquals("foo", val);
    }
}
