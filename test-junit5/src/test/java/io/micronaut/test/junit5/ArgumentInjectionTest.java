
package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
@Property(name = "foo.bar", value = "test")
public class ArgumentInjectionTest {

    @Test
    void testArgumentInjected(
            MathService mathService,
            @Property(name="foo.bar") String val,
            @Client("/") HttpClient client) {
        final int result = mathService.compute(2);

        Assertions.assertEquals(8, result);
        assertNotNull(client);

        assertEquals("test", val);
    }

    @Test
    void testValueArgumentInjected(@Value("${foo.bar}") String val) {
        assertEquals("test", val);
    }

    @Test
    void testExpressionValueArgumentInjected(@Value("#{1 + 1}") Integer val) {
        assertEquals(2, val);
    }
}
