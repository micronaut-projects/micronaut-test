package io.micronaut.test.junit5;

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
public class ArgumentInjectionTest {

    @Test
    void testArgumentInjected(MathService mathService) {
        final int result = mathService.compute(2);

        Assertions.assertEquals(8, result);
    }
}
