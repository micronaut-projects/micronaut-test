package io.micronaut.test.spock

import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.springframework.core.annotation.Order
import spock.lang.Specification

import jakarta.inject.Inject

@MicronautTest(startApplication = false, rebuildContext = true)
class DisableEmbeddedApplicationSpec extends Specification {

    @Inject EmbeddedApplication embeddedApplication

    @Order(1)
    void "test server is not running"() {
        expect:
        !embeddedApplication.isRunning()
    }

    @Order(2)
    void "test server is not running after context rebuild"() {
        expect:
        !embeddedApplication.isRunning()
    }
}
