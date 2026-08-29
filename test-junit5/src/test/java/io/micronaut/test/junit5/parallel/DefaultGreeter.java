package io.micronaut.test.junit5.parallel;

import jakarta.inject.Singleton;

/**
 * Default {@link Greeter} implementation, replaced by a mock in {@link MockBeanFixture}.
 */
@Singleton
class DefaultGreeter implements Greeter {

    @Override
    public String greet() {
        return "real";
    }
}
