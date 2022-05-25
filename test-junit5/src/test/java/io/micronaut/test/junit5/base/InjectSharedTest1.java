
package io.micronaut.test.junit5.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class InjectSharedTest1 extends AbstractSharedTest {

    @BeforeAll
    void setupTest() {
        Assertions.assertNotNull(mathService);
    }

    @Test
    void testMathService() {
        Assertions.assertNotNull(mathService);
    }
}
