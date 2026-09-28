package io.micronaut.test.junit5.lifecycle;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;

/**
 * Records its application context weakly, so the test driving it can tell whether the context is
 * still reachable once the class has finished.
 */
@MicronautTest
@Tag(LifecycleFixtures.TAG)
class ContextRetentionFixture {

    static volatile WeakReference<ApplicationContext> context;

    @Inject
    ApplicationContext applicationContext;

    @Test
    void test() {
        context = new WeakReference<>(applicationContext);
    }
}
