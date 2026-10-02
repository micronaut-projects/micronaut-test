package io.micronaut.test.junit5.lifecycle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * A test class's bean definition is loaded for the rest of the run, and through it the extension
 * stays reachable. The extension must therefore let go of its application context once the class
 * has finished, or every test class in a suite keeps its stopped context alive.
 */
@DisabledInNativeImage
class ContextReleasedTest {

    @Test
    @DisplayName("the application context is unreachable once the test class has finished")
    void contextIsReleased() throws InterruptedException {
        EngineTestKit.engine("junit-jupiter")
            .enableImplicitConfigurationParameters(false)
            .selectors(DiscoverySelectors.selectClass(ContextRetentionFixture.class))
            .execute()
            .testEvents()
            .assertStatistics(stats -> stats.started(1).succeeded(1).failed(0));

        // The server's event loops hold the context until their graceful shutdown ends
        for (int i = 0; i < 100 && ContextRetentionFixture.context.get() != null; i++) {
            System.gc();
            Thread.sleep(100);
        }

        assertNull(ContextRetentionFixture.context.get(), "the stopped application context is still reachable");
    }
}
