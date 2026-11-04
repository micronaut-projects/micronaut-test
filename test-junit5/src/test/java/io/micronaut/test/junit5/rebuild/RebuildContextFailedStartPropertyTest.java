package io.micronaut.test.junit5.rebuild;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineTestKit;

@DisabledInNativeImage
class RebuildContextFailedStartPropertyTest {

    // the build excludes fixtures with this tag, which only run through the test kit here
    static final String TAG = "lifecycle-fixture";

    @Test
    void methodPropertiesOfAFailedRebuildAreNotCarriedOver() {
        EngineTestKit.engine("junit-jupiter")
            .enableImplicitConfigurationParameters(false)
            .selectors(DiscoverySelectors.selectClass(RebuildContextFailedStartPropertyFixture.class))
            .execute()
            .testEvents()
            .assertStatistics(stats -> stats.started(2).succeeded(1).failed(1));
    }
}
