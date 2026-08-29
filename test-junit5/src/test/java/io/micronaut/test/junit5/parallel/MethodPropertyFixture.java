package io.micronaut.test.junit5.parallel;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A method level {@code @Property} mutates the extension's shared {@code testProperties} map and then
 * refreshes the shared {@code Environment}, so overlapping methods see each other's values.
 */
@MicronautTest
@Tag(ParallelFixtures.TAG)
class MethodPropertyFixture {

    @Inject
    Config config;

    @Test
    @Property(name = "parallel.value", value = "alpha")
    void alpha() {
        assertValue("alpha");
    }

    @Test
    @Property(name = "parallel.value", value = "bravo")
    void bravo() {
        assertValue("bravo");
    }

    @Test
    @Property(name = "parallel.value", value = "charlie")
    void charlie() {
        assertValue("charlie");
    }

    @Test
    @Property(name = "parallel.value", value = "delta")
    void delta() {
        assertValue("delta");
    }

    private void assertValue(String expected) {
        assertEquals(expected, config.getValue());
        assertEquals(1, ConcurrencyRecorder.hold("method-property", 30), ParallelFixtures.NO_OVERLAP);
        assertEquals(expected, config.getValue(), "property changed underneath the running test");
    }

    @ConfigurationProperties("parallel")
    static class Config {

        private String value = "unset";

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
