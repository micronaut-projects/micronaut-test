package io.micronaut.test.junit5.parallel;

import io.micronaut.context.annotation.Requires;
import io.micronaut.test.context.TestContext;
import io.micronaut.test.context.TestExecutionListener;
import jakarta.inject.Singleton;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link TestExecutionListener} is a singleton of the test's application context. Any state it
 * keeps between {@code beforeTestMethod} and {@code afterTestMethod} - as Micronaut Data's
 * transaction listener does with its {@code TransactionStatus} - is corrupted once two methods of
 * the class overlap. This listener simply records how deeply the callbacks nest.
 */
@Singleton
@Requires(property = "spec.name", value = "ListenerPairingFixture")
class PairingListener implements TestExecutionListener {

    static final AtomicInteger DEPTH = new AtomicInteger();
    static final AtomicInteger MAX_DEPTH = new AtomicInteger();

    static void reset() {
        DEPTH.set(0);
        MAX_DEPTH.set(0);
    }

    @Override
    public void beforeTestMethod(TestContext testContext) {
        MAX_DEPTH.accumulateAndGet(DEPTH.incrementAndGet(), Math::max);
    }

    @Override
    public void afterTestMethod(TestContext testContext) {
        DEPTH.decrementAndGet();
    }
}
