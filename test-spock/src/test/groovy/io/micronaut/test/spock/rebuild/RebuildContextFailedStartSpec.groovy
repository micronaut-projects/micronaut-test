package io.micronaut.test.spock.rebuild

import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.testkit.engine.EngineTestKit
import spock.lang.Specification

class RebuildContextFailedStartSpec extends Specification {

    void "the method properties of a failed rebuild are not carried over"() {
        given:
        System.setProperty(RebuildContextFailedStartFixture.ENABLED, "true")

        when:
        def events = EngineTestKit.engine("spock")
            .selectors(DiscoverySelectors.selectClass(RebuildContextFailedStartFixture))
            .execute()
            .testEvents()

        then:
        events.started().count() == 2
        events.succeeded().count() == 1
        events.failed().count() == 1

        cleanup:
        System.clearProperty(RebuildContextFailedStartFixture.ENABLED)
    }

    void "the method properties of a test failed by a listener before setup are not carried over"() {
        given:
        System.setProperty(RebuildContextFailedListenerFixture.ENABLED, "true")

        when:
        def events = EngineTestKit.engine("spock")
            .selectors(DiscoverySelectors.selectClass(RebuildContextFailedListenerFixture))
            .execute()
            .testEvents()

        then:
        events.started().count() == 3
        events.succeeded().count() == 2
        events.failed().count() == 1

        cleanup:
        System.clearProperty(RebuildContextFailedListenerFixture.ENABLED)
    }
}
