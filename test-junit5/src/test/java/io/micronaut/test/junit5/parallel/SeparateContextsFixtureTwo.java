package io.micronaut.test.junit5.parallel;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @see SeparateContextsFixtureOne
 */
@MicronautTest
@Tag(ParallelFixtures.TAG)
class SeparateContextsFixtureTwo {

    @Test
    void meetsTheOtherClass() {
        assertTrue(Rendezvous.meet(SeparateContextsFixtureOne.KEY), ParallelFixtures.MUST_MEET);
    }
}
