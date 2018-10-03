package io.micronaut.test.spock

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.*
import spock.lang.*
import javax.inject.Inject

@MicronautTest
class MathCollaboratorSpec extends Specification {

    @Inject
    MathService mathService // <2>

    @Inject
    @Client('/')
    RxHttpClient client // <3>

    @Unroll
    void "should compute #num to #square"() {
        when:
        Integer result = client.toBlocking().retrieve(HttpRequest.GET('/math/compute/10'), Integer) // <3>

        then:
        1 * mathService.compute(10) >> Math.pow(num, 2)  // <4>
        result == square

        where:
        num | square
        2   | 4
        3   | 9
    }

    @MockBean(MathServiceImpl) // <1>
    MathService mathService() {
        Mock(MathService)
    }

}
