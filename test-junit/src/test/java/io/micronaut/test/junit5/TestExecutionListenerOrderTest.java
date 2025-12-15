package io.micronaut.test.junit5;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest(environments = TestExecutionListenerOrderTest.ENVIRONMENT)
public class TestExecutionListenerOrderTest {

    public static final String ENVIRONMENT = "TEST_EXECUTION_LISTENER_ORDER_TEST";

    @Inject
    private FirstExecutionListener firstExecutionListener;
    @Inject
    private SecondExecutionListener secondExecutionListener;

    @Test
    void test() {
        assertNotNull(firstExecutionListener);
        assertNotNull(secondExecutionListener);
    }
}
