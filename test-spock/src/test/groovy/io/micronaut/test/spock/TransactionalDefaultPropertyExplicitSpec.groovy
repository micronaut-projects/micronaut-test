package io.micronaut.test.spock

import io.micronaut.context.ApplicationContext
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(transactional = true)
class TransactionalDefaultPropertyExplicitSpec extends Specification implements TestPropertyProvider {

    @Inject
    ApplicationContext applicationContext

    @Override
    Map<String, String> getProperties() {
        ['micronaut.test.transactional-default': 'false']
    }

    void "explicit transactional annotation wins over default property"() {
        expect:
        applicationContext.environment.getProperty('micronaut.test.transactional', Boolean).orElse(false)
    }
}
