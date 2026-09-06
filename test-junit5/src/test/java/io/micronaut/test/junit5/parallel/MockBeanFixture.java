package io.micronaut.test.junit5.parallel;

import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A {@code @MockBean} is a singleton of the per-class application context, so concurrent methods
 * would stub and verify the very same mock. Each invocation here stubs a distinct answer and then
 * verifies exactly one interaction, which only holds if the invocations do not overlap.
 */
@MicronautTest
@Tag(ParallelFixtures.TAG)
class MockBeanFixture {

    @Inject
    Greeter greeter;

    @ParameterizedTest
    @ValueSource(strings = {"alpha", "bravo", "charlie", "delta", "echo", "foxtrot"})
    void eachInvocationOwnsTheMock(String expected) {
        when(greeter.greet()).thenReturn(expected);
        assertEquals(1, ConcurrencyRecorder.hold("mock-bean", 30), ParallelFixtures.NO_OVERLAP);
        assertEquals(expected, greeter.greet());
        verify(greeter).greet();
    }

    @MockBean(DefaultGreeter.class)
    Greeter greeter() {
        return mock(Greeter.class);
    }
}
