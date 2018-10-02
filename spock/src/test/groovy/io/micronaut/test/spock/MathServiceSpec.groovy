package io.micronaut.test.spock

import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Inject

@MicronautTest
class MathServiceSpec extends Specification {

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

    @MockBean(MathServiceImpl)
    MathService mathService() {
        Mock(MathService)
    }
}
