package io.micronaut.test.junit5.lifecycle;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * One test per outcome. Run only by {@link TestOutcomeListenerTest}.
 */
@MicronautTest
@Property(name = "spec.name", value = "TestOutcomeFixture")
@Tag(LifecycleFixtures.TAG)
class TestOutcomeFixture {

    @Inject
    Counter counter;

    @Test
    void passes() {
        assertEquals(1, counter.value());
    }

    @Test
    void fails() {
        fail("expected failure");
    }

    @Test
    void aborts() {
        assertEquals(1, counter.value());
        // Aborting is the outcome under test. The assertion above runs first, so the fixture is
        // still checked to be wired up before the assumption ends the test.
        Assumptions.assumeTrue(false, "aborted on purpose");
    }

    @Test
    @Disabled("disabled on purpose")
    void isDisabled() {
        fail("a disabled test must never run");
    }
}
