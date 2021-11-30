
package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@CustomBuilderMicronautTest
public class CustomContextBuilderTest {
    @Property(name = "custom.builder.prop")
    String val;

    @Test
    void testCustomBuilderIsUsed() {
        Assertions.assertEquals("value", val);
    }
}
