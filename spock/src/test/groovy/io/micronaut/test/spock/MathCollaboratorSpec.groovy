package io.micronaut.test.spock

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Inject

@MicronautTest
class MathCollaboratorSpec extends Specification {

    @Inject
    MathService mathService

    @Inject
    @Client('/')
    RxHttpClient client

    @Unroll
    void "should compute #num to #square"() {
        when:
        def result = client.toBlocking().retrieve(HttpRequest.GET('/math/compute/10'), Integer)

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
