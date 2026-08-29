package io.micronaut.test.junit5.parallel;

/**
 * Shared constants for the parallel-execution fixtures.
 */
final class ParallelFixtures {

    /**
     * Fixtures carry this tag so that the normal build excludes them; they are only ever run from
     * {@code MicronautTestParallelExecutionTest} through the JUnit Platform test kit.
     */
    static final String TAG = "parallel-fixture";

    /**
     * Asserted by every fixture that must not overlap, so a failure names the method that did.
     */
    static final String NO_OVERLAP = "another execution sharing this application context ran at the same time";

    /**
     * Asserted by every fixture that must overlap.
     */
    static final String MUST_MEET = "the other participants never arrived, so these executions did not overlap";

    private ParallelFixtures() {
    }
}
