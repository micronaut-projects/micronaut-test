package io.micronaut.test.leak;

import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

class LeakPresenceExtensionTest {
    @Test
    public void jupiter() {
        EngineTestKit
            .engine("junit-jupiter")
            .selectors(selectClass(LeakyTest.class))
            .execute()
            .containerEvents()
            .assertStatistics(stats -> stats
                .started(2)
                .succeeded(1)
                .failed(1)
            );
    }

    @Test
    public void jupiterNested() {
        // the nested classes share the enclosing class's scope, so the leak fails only the enclosing class
        EngineTestKit
            .engine("junit-jupiter")
            .selectors(selectClass(LeakyNestedTest.class))
            .execute()
            .containerEvents()
            .assertStatistics(stats -> stats
                .started(4)
                .succeeded(3)
                .failed(1)
            );
    }

    @Test
    public void spock() {
        EngineTestKit
            .engine("spock")
            .selectors(selectClass("io.micronaut.test.leak.LeakySpec"))
            .execute()
            .containerEvents()
            .assertStatistics(stats -> stats
                .started(2)
                .succeeded(1)
                .failed(1)
            );
    }
}
