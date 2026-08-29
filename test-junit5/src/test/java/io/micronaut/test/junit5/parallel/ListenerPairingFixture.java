package io.micronaut.test.junit5.parallel;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @see PairingListener
 */
@MicronautTest
@Tag(ParallelFixtures.TAG)
@Property(name = "spec.name", value = "ListenerPairingFixture")
class ListenerPairingFixture {

    static final String KEY = "listener-pairing";

    @Test
    void one() {
        assertEquals(1, ConcurrencyRecorder.hold(KEY, 40), ParallelFixtures.NO_OVERLAP);
    }

    @Test
    void two() {
        assertEquals(1, ConcurrencyRecorder.hold(KEY, 40), ParallelFixtures.NO_OVERLAP);
    }

    @Test
    void three() {
        assertEquals(1, ConcurrencyRecorder.hold(KEY, 40), ParallelFixtures.NO_OVERLAP);
    }

    @Test
    void four() {
        assertEquals(1, ConcurrencyRecorder.hold(KEY, 40), ParallelFixtures.NO_OVERLAP);
    }
}
