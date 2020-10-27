
package io.micronaut.test.junit5;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

class MathInheritedTest extends BaseTest {

    @Inject MathService mathService;

    @Test
    void testComputeNumToSquare() {
        final Integer result = mathService.compute(2);

        Assertions.assertEquals((Integer) 8, result);
    }
}
