package io.micronaut.test.junit5.parallel;

import jakarta.inject.Singleton;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A singleton whose identity distinguishes one application context from the next, used to detect a
 * test holding beans from a context that has already been torn down and rebuilt.
 */
@Singleton
class ContextIdentity {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    private final int id = SEQUENCE.incrementAndGet();

    int getId() {
        return id;
    }
}
