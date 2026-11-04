package io.micronaut.test.junit5;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Around advice on a class-level method applies when each test rebuilds the context too.
 */
@MicronautTest(rebuildContext = true, transactional = false)
@Property(name = "datasources.default.url", value = "jdbc:h2:mem:ClassLifecycleAdviceRebuildTest;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
@DbProperties
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClassLifecycleAdviceRebuildTest {

    @Inject
    MathService mathService;

    @Inject
    ApplicationContext context;

    @BeforeAll
    @ClassLifecycleAdviceTest.Recorded
    void adviseMeWithRebuild() {
        assertNotNull(mathService);
    }

    @Test
    void beforeAllIsAdvised() {
        assertTrue(context.isRunning(), "the proxied test instance is injected from the rebuilt context");
        assertEquals(ClassLifecycleAdviceRebuildTest.class, ClassLifecycleAdviceTest.RecordingInterceptor.TARGETS.get("adviseMeWithRebuild"));
    }

    @Test
    void anotherTestStillSeesTheAdvice() {
        assertTrue(context.isRunning(), "the proxied test instance is injected from the rebuilt context");
        assertEquals(ClassLifecycleAdviceRebuildTest.class, ClassLifecycleAdviceTest.RecordingInterceptor.TARGETS.get("adviseMeWithRebuild"));
    }
}
