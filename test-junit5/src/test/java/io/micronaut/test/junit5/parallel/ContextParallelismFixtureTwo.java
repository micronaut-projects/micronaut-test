package io.micronaut.test.junit5.parallel;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * One of two classes whose application contexts {@link ActiveContextProbe} counts, to check the
 * {@code micronaut.test.context.parallelism} limit.
 */
@MicronautTest
@Tag(ParallelFixtures.TAG)
@Property(name = "spec.name", value = ActiveContextProbe.SPEC)
class ContextParallelismFixtureTwo {

    @Inject
    ActiveContextProbe probe;

    @Test
    void contextStarts() {
        assertNotNull(probe);
    }
}
