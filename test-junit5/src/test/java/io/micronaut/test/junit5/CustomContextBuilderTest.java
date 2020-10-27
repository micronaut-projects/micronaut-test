
package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

@CustomBuilderMicronautTest
public class CustomContextBuilderTest {
    @Property(name = "custom.builder.prop")
    String val;

    @Test
    void testCustomBuilderIsUsed() {
        Assert.assertEquals("value", val);
    }
}
