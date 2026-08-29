package io.micronaut.test.junit5.lifecycle;

import jakarta.inject.Singleton;

/**
 * A trivial collaborator, so the fixture is genuinely injected.
 */
@Singleton
class Counter {

    int value() {
        return 1;
    }
}
