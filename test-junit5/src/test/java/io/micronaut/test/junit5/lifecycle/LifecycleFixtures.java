package io.micronaut.test.junit5.lifecycle;

/**
 * Shared constants for the lifecycle fixtures.
 */
final class LifecycleFixtures {

    /**
     * Fixtures carry this tag so the normal build skips them; they are executed only by the tests in
     * this package, through the JUnit Platform test kit.
     */
    static final String TAG = "lifecycle-fixture";

    private LifecycleFixtures() {
    }
}
