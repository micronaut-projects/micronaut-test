package io.micronaut.test.leak;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;
import org.junit.platform.testkit.engine.Events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        Events containers = EngineTestKit
            .engine("junit-jupiter")
            .selectors(selectClass(LeakyNestedTest.class))
            .execute()
            .containerEvents();
        containers.assertStatistics(stats -> stats
            .started(4)
            .succeeded(3)
            .failed(1)
        );
        Event failure = containers.failed().list().get(0);
        assertEquals(LeakyNestedTest.class.getSimpleName(), failure.getTestDescriptor().getDisplayName());
        Throwable cause = failure.getRequiredPayload(TestExecutionResult.class).getThrowable().orElseThrow();
        assertInstanceOf(IllegalStateException.class, cause, () -> "not the leak failure: " + cause);
        assertTrue(cause.getMessage().contains("Possible memory leak"), () -> "not the leak failure: " + cause);
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
