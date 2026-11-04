package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    @BeforeAll
    @ClassLifecycleAdviceTest.Recorded
    void adviseMeWithRebuild() {
        assertNotNull(mathService);
    }

    @Test
    void beforeAllIsAdvised() {
        assertEquals(ClassLifecycleAdviceRebuildTest.class, ClassLifecycleAdviceTest.RecordingInterceptor.TARGETS.get("adviseMeWithRebuild"));
    }

    @Test
    void anotherTestStillSeesTheAdvice() {
        assertEquals(ClassLifecycleAdviceRebuildTest.class, ClassLifecycleAdviceTest.RecordingInterceptor.TARGETS.get("adviseMeWithRebuild"));
    }
}
