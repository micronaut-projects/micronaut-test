
package io.micronaut.test.junit.intercept;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit.annotation.MicronautTest;
import io.micronaut.test.junit.MathService;
import jakarta.inject.Inject;

@Property(name = "InterceptTestSpec", value = "true")
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InterceptTestTest {

    @Inject
    MathService mathService;

    @Inject
    TestInterceptor testInterceptor;

    @BeforeEach
    void myBeforeTest() {
    }

    @AfterEach
    void myAfterEach() {
    }

    @ParameterizedTest
    @CsvSource({"2,8", "3,12"})
    void testComputeNumToSquare(Integer num, Integer square) {
        final Integer result = mathService.compute(num);

        assertEquals(
                square,
                result
        );
    }

    @Test
    void testOk() {
        assertTrue(true);
    }

    @Test
    void testInvocations() {
        List<String> calls = new ArrayList<>(testInterceptor.calls);
        List<String> expected = Arrays.asList(
                "IN BEFORE myBeforeTest",
                "OUT BEFORE myBeforeTest",
                "IN testOk",
                "OUT testOk",
                "IN AFTER myAfterEach",
                "OUT AFTER myAfterEach",
                "IN BEFORE myBeforeTest",
                "OUT BEFORE myBeforeTest",
                "IN testComputeNumToSquare",
                "OUT testComputeNumToSquare",
                "IN AFTER myAfterEach",
                "OUT AFTER myAfterEach",
                "IN BEFORE myBeforeTest",
                "OUT BEFORE myBeforeTest",
                "IN testComputeNumToSquare",
                "OUT testComputeNumToSquare",
                "IN AFTER myAfterEach",
                "OUT AFTER myAfterEach",
                "IN BEFORE myBeforeTest",
                "OUT BEFORE myBeforeTest",
                "IN testInvocations"
        );
        Assertions.assertEquals(expected.size(), calls.size());
        for (int i = 0; i < expected.size(); i++) {
            String a = calls.get(i);
            String b = expected.get(i);
            Assertions.assertEquals(b, a);
        }
    }
}
