
package io.micronaut.test.junit5;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
public class ConstructorInjectionTest {
    private final MathService mathService;

    public ConstructorInjectionTest(MathService mathService) {
        this.mathService = mathService;
    }

    @Test
    void testConstructorInjected() {
        final int result = mathService.compute(2);

        Assertions.assertEquals(8, result);
    }
}
