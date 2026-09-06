package io.micronaut.test.junit5.parallel;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Paired with {@link SeparateContextsFixtureTwo} to prove that serialising one context does not
 * serialise the whole suite: two different {@code @MicronautTest} classes must still overlap.
 */
@MicronautTest
@Tag(ParallelFixtures.TAG)
class SeparateContextsFixtureOne {

    static final String KEY = "separate-contexts";

    @Test
    void meetsTheOtherClass() {
        assertTrue(Rendezvous.meet(KEY), ParallelFixtures.MUST_MEET);
    }
}
