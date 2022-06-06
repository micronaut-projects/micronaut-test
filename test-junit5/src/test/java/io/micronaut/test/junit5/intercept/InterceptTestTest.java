
package io.micronaut.test.junit5.intercept;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.junit5.MathService;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        Assertions.assertEquals(
                square,
                result
        );
    }

    @Test
    void testOk() {
    }

    @Test
    void testInvocations() {
        List<String> calls = new ArrayList<>(testInterceptor.calls);
        List<String> expected = Arrays.asList(
                "IN BEFORE testOk",
                "OUT BEFORE testOk",
                "IN testOk",
                "OUT testOk",
                "IN AFTER testOk",
                "OUT AFTER testOk",
                "IN BEFORE testComputeNumToSquare",
                "OUT BEFORE testComputeNumToSquare",
                "IN testComputeNumToSquare",
                "OUT testComputeNumToSquare",
                "IN AFTER testComputeNumToSquare",
                "OUT AFTER testComputeNumToSquare",
                "IN BEFORE testComputeNumToSquare",
                "OUT BEFORE testComputeNumToSquare",
                "IN testComputeNumToSquare",
                "OUT testComputeNumToSquare",
                "IN AFTER testComputeNumToSquare",
                "OUT AFTER testComputeNumToSquare",
                "IN BEFORE testInvocations",
                "OUT BEFORE testInvocations",
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
