package io.micronaut.test.junit5.lifecycle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineTestKit;

@DisabledInNativeImage
class RebuildContextFailedStartTest {

    @Test
    @DisplayName("a failed rebuild for the first test does not leave the failed context to the next test")
    void nextTestRebuildsAfterAFailedRebuild() {
        RebuildContextFailedStartFixture.FailsOnSecondStart.STARTS.set(0);

        EngineTestKit.engine("junit-jupiter")
            .enableImplicitConfigurationParameters(false)
            .selectors(DiscoverySelectors.selectClass(RebuildContextFailedStartFixture.class))
            .execute()
            .testEvents()
            .assertStatistics(stats -> stats.started(2).succeeded(1).failed(1));
    }
}
