package io.micronaut.test.spock

import io.micronaut.context.ApplicationContext
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Specification

import jakarta.inject.Inject

@MicronautTest(transactional = false)
class CleanupSpecRunningApplicationSpec extends Specification {

    @Shared
    @Inject
    ApplicationContext applicationContext

    void cleanupSpec() {
        assert applicationContext != null
        assert applicationContext.isRunning()
    }

    void "application remains running until cleanupSpec completes"() {
        expect:
        applicationContext.isRunning()
    }
}
