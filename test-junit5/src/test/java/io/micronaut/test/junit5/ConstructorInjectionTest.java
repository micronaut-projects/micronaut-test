
package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeAll;

@MicronautTest
@Property(name = "foo.bar", value = "test")
public class ConstructorInjectionTest {
    private final MathService mathService;
    private final HttpClient client;
    private final String val;

    @BeforeAll
    static void injectStatic(MathService mathService) {
        Assertions.assertNotNull(mathService);
    }


    public ConstructorInjectionTest(
            @Property(name="foo.bar") String val,
            MathService mathService,
            @Client("/") HttpClient client) {
        this.mathService = mathService;
        this.client = client;
        this.val = val;
    }

    @Test
    void testConstructorInjected() {
        final int result = mathService.compute(2);

        assertEquals(8, result);
        assertNotNull(client);

        assertEquals("test", val);
    }
}
