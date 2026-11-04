package io.micronaut.test.junit5.parallel;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.context.TestContext;
import io.micronaut.test.context.TestExecutionListener;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * A class whose {@code afterTestClass} listener fails, which must not keep its
 * {@code micronaut.test.context.parallelism} permit from the class that runs after it.
 */
@MicronautTest
@Tag(ParallelFixtures.TAG)
@Property(name = "spec.name", value = ContextParallelismFailingCleanupFixture.SPEC)
class ContextParallelismFailingCleanupFixture {

    static final String SPEC = "ContextParallelismFailingCleanup";

    @Test
    void contextStarts() {
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC)
    static class FailingCleanupListener implements TestExecutionListener {

        static final String MESSAGE = "afterTestClass failed";

        @Override
        public void afterTestClass(TestContext testContext) {
            throw new IllegalStateException(MESSAGE);
        }
    }
}
