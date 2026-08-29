package io.micronaut.test.junit5.lifecycle;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A test class that opens something in {@code @PostConstruct} has to be able to close it again.
 */
@MicronautTest
@Tag(LifecycleFixtures.TAG)
class PreDestroyFixture {

    static final List<String> EVENTS = Collections.synchronizedList(new ArrayList<>());

    @Inject
    Counter counter;

    @PostConstruct
    void open() {
        EVENTS.add("postConstruct");
    }

    @PreDestroy
    void close() {
        EVENTS.add("preDestroy");
    }

    @Test
    void first() {
        EVENTS.add("test:first");
        assertTrue(EVENTS.contains("postConstruct"), "the instance was not initialised before the test");
        assertEquals(1, counter.value());
    }

    @Test
    void second() {
        EVENTS.add("test:second");
        assertTrue(EVENTS.contains("postConstruct"), "the instance was not initialised before the test");
        assertEquals(1, counter.value());
    }
}
