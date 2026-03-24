package io.micronaut.test.spock

import io.micronaut.context.env.Environment
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(deduceEnvironment = false)
class DisableEnvironmentDeductionSpec extends Specification {

    @Inject
    Environment environment

    void "environment deduction property is false"() {
        expect:
            environment.getProperty(Environment.DEDUCE_ENVIRONMENT_PROPERTY, Boolean).orElse(null) == false
    }
}
