package io.micronaut.test.spock

import io.micronaut.context.ApplicationContext
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class TransactionalDefaultPropertySpec extends Specification implements TestPropertyProvider {

    @Inject
    ApplicationContext applicationContext

    @Override
    Map<String, String> getProperties() {
        ['micronaut.test.transactional-default': 'false']
    }

    void "transactions disabled when default property false"() {
        expect:
        !applicationContext.environment.getProperty('micronaut.test.transactional', Boolean).orElse(true)
    }
}
