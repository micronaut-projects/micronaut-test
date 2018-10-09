package io.micronaut.test.spock

import io.micronaut.context.annotation.Factory
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import spock.lang.Specification
import spock.lang.Unroll
import spock.mock.DetachedMockFactory

import javax.inject.Inject

@MicronautTest
class DetachedMathServiceSpec extends Specification {


    @Inject
    MathService mathService

    @Unroll
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

        @MockBean(MathServiceImpl)
        MathService mathService() {
            mockFactory.Mock(MathService)
        }
    }
}
