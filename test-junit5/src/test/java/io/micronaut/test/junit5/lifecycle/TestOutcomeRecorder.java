package io.micronaut.test.junit5.lifecycle;

import io.micronaut.context.annotation.Requires;
import io.micronaut.test.context.TestContext;
import io.micronaut.test.context.TestExecutionListener;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Records the outcome callbacks a {@link TestExecutionListener} receives.
 */
@Singleton
@Requires(property = "spec.name", value = "TestOutcomeFixture")
public class TestOutcomeRecorder implements TestExecutionListener {

    private static final List<String> EVENTS = Collections.synchronizedList(new ArrayList<>());

    public static void reset() {
        EVENTS.clear();
    }

    public static List<String> events() {
        synchronized (EVENTS) {
            return List.copyOf(EVENTS);
        }
    }

    @Override
    public void testSuccessful(TestContext testContext) {
        EVENTS.add("successful:" + testContext.getTestName());
    }

    @Override
    public void testFailed(TestContext testContext) {
        EVENTS.add("failed:" + testContext.getTestName() + ":"
            + testContext.getTestException().getClass().getSimpleName());
    }

    @Override
    public void testAborted(TestContext testContext) {
        EVENTS.add("aborted:" + testContext.getTestName() + ":"
            + testContext.getTestException().getClass().getSimpleName());
    }

    @Override
    public void testDisabled(TestContext testContext, String reason) {
        EVENTS.add("disabled:" + testContext.getTestName() + ":" + reason);
    }
}
