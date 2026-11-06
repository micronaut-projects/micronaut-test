package io.micronaut.test.junit5;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A static {@code @BeforeAll} method runs before any instance exists, but it can still receive
 * beans as method parameters.
 */
@MicronautTest
class StaticBeforeAllParameterTest {

    static Integer computedInBeforeAll;

    @BeforeAll
    static void computeOnce(MathService mathService) {
        computedInBeforeAll = mathService.compute(3);
    }

    @Test
    void usesTheValueFromBeforeAll() {
        assertEquals(12, computedInBeforeAll);
    }
}
