package io.micronaut.test.junit5;

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
public class Simple2Test extends SimpleBaseTest {

    @Inject
    SimpleService simpleService;

    @Test
    void testComputeNumToSquare() {
        assertNotNull(simpleService);
    }
}
