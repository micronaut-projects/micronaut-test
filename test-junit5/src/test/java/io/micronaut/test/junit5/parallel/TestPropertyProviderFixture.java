package io.micronaut.test.junit5.parallel;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link TestPropertyProvider} requires the {@code PER_CLASS} lifecycle, which means one test
 * instance is shared by every method of the class.
 */
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag(ParallelFixtures.TAG)
class TestPropertyProviderFixture implements TestPropertyProvider {

    static final String KEY = "test-property-provider";

    @Inject
    ApplicationContext applicationContext;

    @Override
    public Map<String, String> getProperties() {
        return Map.of("parallel.provided", "provided-value");
    }

    @Test
    void one() {
        assertProvidedProperty();
    }

    @Test
    void two() {
        assertProvidedProperty();
    }

    @Test
    void three() {
        assertProvidedProperty();
    }

    private void assertProvidedProperty() {
        assertEquals(1, ConcurrencyRecorder.hold(KEY, 30), ParallelFixtures.NO_OVERLAP);
        assertEquals("provided-value",
            applicationContext.getEnvironment().getRequiredProperty("parallel.provided", String.class));
    }
}
