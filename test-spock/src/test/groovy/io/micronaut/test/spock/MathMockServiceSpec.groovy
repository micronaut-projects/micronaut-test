
package io.micronaut.test.spock

import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import spock.lang.Unroll

import jakarta.inject.Inject

@MicronautTest
class MathMockServiceSpec extends Specification {

    @Inject
    MathService mathService // <3>

    @Unroll
    void "should compute #num to #square"() {
        when:
        def result = mathService.compute(num)

        then:
        1 * mathService.compute(num) >> Math.pow(num, 2)  // <4>
        result == square

        where:
        num | square
        2   | 4
        3   | 9
    }

    @MockBean(MathServiceImpl) // <1>
    MathService mathService() {
        Mock(MathService) // <2>
    }
}
