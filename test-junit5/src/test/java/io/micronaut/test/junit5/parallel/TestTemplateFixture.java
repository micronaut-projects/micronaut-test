package io.micronaut.test.junit5.parallel;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Parameterized tests, repeated tests and dynamic tests reach the extension through
 * {@code interceptTestTemplateMethod} and {@code interceptTestFactoryMethod} rather than
 * {@code interceptTestMethod}; their invocations must be serialised on the same terms.
 */
@MicronautTest
@Tag(ParallelFixtures.TAG)
class TestTemplateFixture {

    static final String KEY = "test-template";

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void parameterized(int value) {
        assertEquals(1, ConcurrencyRecorder.hold(KEY, 40), ParallelFixtures.NO_OVERLAP);
    }

    @RepeatedTest(4)
    void repeated() {
        assertEquals(1, ConcurrencyRecorder.hold(KEY, 40), ParallelFixtures.NO_OVERLAP);
    }

    @TestFactory
    List<DynamicTest> dynamic() {
        return IntStream.range(0, 4)
            .mapToObj(i -> DynamicTest.dynamicTest("dynamic-" + i, () -> assertEquals(1, ConcurrencyRecorder.hold(KEY, 40), ParallelFixtures.NO_OVERLAP)))
            .toList();
    }
}
