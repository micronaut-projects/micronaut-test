package io.micronaut.test.spock

import io.micronaut.context.ApplicationContext
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Specification

import jakarta.inject.Inject

class AbstractExample extends Specification {

    @Inject
    @Shared
    ApplicationContext sharedCtx

    @Inject
    ApplicationContext ctx

}

@MicronautTest
class FailingTest extends AbstractExample {

    def 'injection is not null'() {
        expect:
        ctx != null
    }

    def 'shared injection is not null'() {
        expect:
        sharedCtx != null
    }
}

@MicronautTest
class SuccessfulTest extends AbstractExample {

    @Shared
    @Inject
    ApplicationContext dummy

    def 'injection is not null'() {
        expect:
        ctx != null
    }

    def 'shared injection is not null'() {
        expect:
        sharedCtx != null
    }
}
