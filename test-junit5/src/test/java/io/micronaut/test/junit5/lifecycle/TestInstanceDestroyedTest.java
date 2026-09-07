package io.micronaut.test.junit5.lifecycle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The test instance is a managed object: if Micronaut calls {@code @PostConstruct} on it, it has to
 * call {@code @PreDestroy} too.
 */
// These drive fixtures through a second JUnit Platform launcher, selecting them by class at
// run time. A native image resolves its tests at build time from a fixed list, so a nested
// launcher discovers nothing.
@DisabledInNativeImage
class TestInstanceDestroyedTest {

    @Test
    @DisplayName("@PreDestroy runs once per test instance")
    void testInstancesAreDestroyed() {
        PreDestroyFixture.EVENTS.clear();

        EngineTestKit.engine("junit-jupiter")
            .enableImplicitConfigurationParameters(false)
            .selectors(DiscoverySelectors.selectClass(PreDestroyFixture.class))
            .execute()
            .testEvents()
            .assertStatistics(stats -> stats.started(2).succeeded(2).failed(0));

        List<String> events = List.copyOf(PreDestroyFixture.EVENTS);

        assertEquals(2, events.stream().filter("postConstruct"::equals).count(), events::toString);
        assertEquals(2, events.stream().filter("preDestroy"::equals).count(),
            () -> "@PreDestroy was not called for every test instance: " + events);
        assertEquals(2, events.stream().filter(e -> e.startsWith("test:")).count(), events::toString);
    }
}
