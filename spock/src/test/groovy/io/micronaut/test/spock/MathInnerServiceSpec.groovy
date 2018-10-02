package io.micronaut.test.spock

import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import spock.lang.Specification

import javax.inject.Inject

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

    @MockBean(MathServiceImpl)
    static class MyMock implements MathService {

        @Override
        Integer compute(Integer num) {
            return 50
        }
    }
}

