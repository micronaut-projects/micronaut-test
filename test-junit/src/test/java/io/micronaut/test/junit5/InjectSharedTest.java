
package io.micronaut.test.junit5;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import jakarta.inject.Inject;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InjectSharedTest {

    @Inject
    MathService mathService;

    @BeforeAll
    void setupTest() {
        Assertions.assertNotNull(mathService);
    }

    @Test
    void testMathService() {
        Assertions.assertNotNull(mathService);
    }
}
