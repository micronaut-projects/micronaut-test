
package io.micronaut.test.junit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

class MathInheritedTest extends BaseTest {

    @Inject MathService mathService;

    @Test
    void testComputeNumToSquare() {
        final Integer result = mathService.compute(2);

        Assertions.assertEquals((Integer) 8, result);
    }
}
