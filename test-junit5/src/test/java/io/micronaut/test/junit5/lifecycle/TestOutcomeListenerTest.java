package io.micronaut.test.junit5.lifecycle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A {@code TestExecutionListener} sees every outcome, including the two that previously never
 * reached it at all.
 */
// These drive fixtures through a second JUnit Platform launcher, selecting them by class at
// run time. A native image resolves its tests at build time from a fixed list, so a nested
// launcher discovers nothing.
@DisabledInNativeImage
class TestOutcomeListenerTest {

    @Test
    @DisplayName("every outcome reaches the listener")
    void everyOutcomeIsReported() {
        TestOutcomeRecorder.reset();

        EngineTestKit.engine("junit-jupiter")
            .enableImplicitConfigurationParameters(false)
            .selectors(org.junit.platform.engine.discovery.DiscoverySelectors.selectClass(TestOutcomeFixture.class))
            .execute()
            .testEvents()
            .assertStatistics(stats -> stats.succeeded(1).failed(1).aborted(1).skipped(1));

        List<String> events = TestOutcomeRecorder.events();

        assertTrue(events.contains("successful:passes()"), events::toString);
        assertTrue(events.contains("failed:fails():AssertionFailedError"), events::toString);
        assertTrue(events.contains("aborted:aborts():TestAbortedException"), events::toString);
        assertTrue(events.contains("disabled:isDisabled():disabled on purpose"), events::toString);
        assertEquals(4, events.size(), events::toString);
    }
}
