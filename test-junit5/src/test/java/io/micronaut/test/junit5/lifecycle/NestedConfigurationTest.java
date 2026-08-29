package io.micronaut.test.junit5.lifecycle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Configuration declared on a {@code @Nested} class has nowhere to go, because the nested class
 * shares its enclosing class's application context. Saying so beats ignoring it.
 */
// These drive fixtures through a second JUnit Platform launcher, selecting them by class at
// run time. A native image resolves its tests at build time from a fixed list, so a nested
// launcher discovers nothing.
@DisabledInNativeImage
class NestedConfigurationTest {

    @Test
    @DisplayName("@MicronautTest on a @Nested class is rejected with an explanation")
    void nestedMicronautTestIsRejected() {
        String message = failureMessage(NestedMicronautTestFixture.class);

        assertTrue(message.contains("@MicronautTest cannot be declared on the @Nested class"), message);
        assertTrue(message.contains(NestedMicronautTestFixture.Inner.class.getName()), message);
        assertTrue(message.contains(NestedMicronautTestFixture.class.getName()), message);
    }

    @Test
    @DisplayName("@Property on a @Nested class is rejected with an explanation")
    void nestedPropertyIsRejected() {
        String message = failureMessage(NestedPropertyFixture.class);

        assertTrue(message.contains("@Property cannot be declared on the @Nested class"), message);
        assertTrue(message.contains(NestedPropertyFixture.Inner.class.getName()), message);
    }

    @Test
    @DisplayName("a @Nested class that declares no configuration still works")
    void plainNestedClassesStillWork() {
        run(PlainNestedFixture.class).testEvents()
            .assertStatistics(stats -> stats.started(2).succeeded(2).failed(0));
    }

    private static String failureMessage(Class<?> fixture) {
        Events containers = run(fixture).containerEvents();
        containers.assertStatistics(stats -> stats.failed(1));
        return containers.failed().stream()
            .flatMap(event -> event.getPayload(org.junit.platform.engine.TestExecutionResult.class).stream())
            .flatMap(result -> result.getThrowable().stream())
            .map(Throwable::getMessage)
            .findFirst()
            .orElseThrow(() -> new AssertionError("No container failure was recorded"));
    }

    private static org.junit.platform.testkit.engine.EngineExecutionResults run(Class<?> fixture) {
        return EngineTestKit.engine("junit-jupiter")
            .enableImplicitConfigurationParameters(false)
            .selectors(DiscoverySelectors.selectClass(fixture))
            .execute();
    }
}
