package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

class MicronautTestContextParallelismTest {

    private static final String CONTEXT_PARALLELISM = "micronaut.test.context.parallelism";

    @AfterEach
    void clearState() {
        System.clearProperty(CONTEXT_PARALLELISM);
        ParallelContextProbe.reset();
    }

    @Test
    void limitsActiveMicronautTestContextsAcrossParallelJUnitClasses() {
        System.setProperty(CONTEXT_PARALLELISM, "1");

        TestExecutionSummary summary = executeInParallel(
            selectClass(ParallelContextMicronautTestOne.class),
            selectClass(ParallelContextMicronautTestTwo.class)
        );

        assertTrue(summary.failures().isEmpty(), () -> "Expected no failures but saw: " + summary.failures());
        assertTrue(ParallelContextProbe.maxActiveContexts() <= 1,
            () -> "Expected at most one active context, saw " + ParallelContextProbe.maxActiveContexts());
    }

    private static TestExecutionSummary executeInParallel(DiscoverySelector... selectors) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectors)
            .configurationParameter("junit.jupiter.execution.parallel.enabled", "true")
            .configurationParameter("junit.jupiter.execution.parallel.mode.default", "same_thread")
            .configurationParameter("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")
            .configurationParameter("junit.jupiter.execution.parallel.config.strategy", "fixed")
            .configurationParameter("junit.jupiter.execution.parallel.config.fixed.parallelism", "2")
            .build();

        Launcher launcher = LauncherFactory.create();
        TestExecutionSummary summary = new TestExecutionSummary();
        launcher.registerTestExecutionListeners(summary);
        launcher.execute(request);
        return summary;
    }

    private static final class TestExecutionSummary implements TestExecutionListener {
        private final List<String> failures = new CopyOnWriteArrayList<>();

        @Override
        public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
            testExecutionResult.getThrowable().ifPresent(throwable ->
                failures.add(testIdentifier.getDisplayName() + ": " + throwable.getMessage())
            );
        }

        List<String> failures() {
            return failures;
        }
    }
}

@MicronautTest
class ParallelContextMicronautTestOne {

    @Inject
    ParallelContextVerifier verifier;

    @Test
    void testContextStarts() {
        assertTrue(verifier.isPresent());
    }
}

@MicronautTest
class ParallelContextMicronautTestTwo {

    @Inject
    ParallelContextVerifier verifier;

    @Test
    void testContextStarts() {
        assertTrue(verifier.isPresent());
    }
}

@Context
@Singleton
class ParallelContextProbe {
    private static final AtomicInteger ACTIVE_CONTEXTS = new AtomicInteger();
    private static final AtomicInteger MAX_ACTIVE_CONTEXTS = new AtomicInteger();

    ParallelContextProbe() {
        int activeContexts = ACTIVE_CONTEXTS.incrementAndGet();
        MAX_ACTIVE_CONTEXTS.accumulateAndGet(activeContexts, Math::max);
        if (activeContexts > 1) {
            throw new IllegalStateException("Detected " + activeContexts + " active Micronaut test contexts");
        }
        try {
            Thread.sleep(Duration.ofMillis(250));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while simulating slow startup", e);
        }
    }

    @PreDestroy
    void close() {
        ACTIVE_CONTEXTS.decrementAndGet();
    }

    static int maxActiveContexts() {
        return MAX_ACTIVE_CONTEXTS.get();
    }

    static void reset() {
        ACTIVE_CONTEXTS.set(0);
        MAX_ACTIVE_CONTEXTS.set(0);
    }
}

@Singleton
@Requires(beans = ParallelContextProbe.class)
class ParallelContextVerifier {

    boolean isPresent() {
        return true;
    }
}
