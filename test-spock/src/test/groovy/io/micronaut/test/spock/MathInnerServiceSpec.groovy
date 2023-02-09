
package io.micronaut.test.spock

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import spock.lang.Specification

import jakarta.inject.Inject

@MicronautTest
class MathInnerServiceSpec extends Specification {

    @Inject
    MathService mathService

    void "should compute use inner mock"() {
        when:
        def result = mathService.compute(10)

        then:
        result == 50
    }

    @MockBean(MathService)
    static class MyMock implements MathService {

        @Override
        Integer compute(Integer num) {
            return 50
        }
    }
}

