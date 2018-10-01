package io.micronaut.test.spock

import groovy.transform.NotYetImplemented
import io.micronaut.context.annotation.Factory
import io.micronaut.test.spock.annotation.MicronautTest
import io.micronaut.test.spock.annotation.MockBean
import spock.lang.Specification
import spock.lang.Unroll
import spock.mock.DetachedMockFactory

import javax.inject.Inject

@MicronautTest

class MathServiceSpec extends Specification {


    @Inject
    MathService mathService

    @Unroll
    @NotYetImplemented
    void "should compute #num to #square"() {
        when:
        def result = mathService.compute(num)

        then:
        1 * mathService.compute(_) >> { Math.pow(num, 2) }
        result == square

        where:
        num || square
        2   || 4
        3   || 9
    }


    @Factory
    static class Mocks {
        DetachedMockFactory mockFactory = new DetachedMockFactory()

        @MockBean(MathService)
        MathService mathService() {
            mockFactory.Mock(MathService)
        }
    }
}
