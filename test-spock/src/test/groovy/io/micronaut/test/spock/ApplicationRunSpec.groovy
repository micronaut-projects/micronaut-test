package io.micronaut.test.spock


import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class ApplicationRunSpec extends Specification {

    @Inject
    @Client("/")
    RxHttpClient client

    void "test ping server"() {
        expect:
        client.toBlocking().retrieve(HttpRequest.GET('/test'), String) == "mocked by " +ApplicationRunSpec.name
    }

    void "test ping server again"() {
        expect:
        client.toBlocking().retrieve(HttpRequest.GET('/test'), String) == "mocked by " +ApplicationRunSpec.name
    }

    @MockBean(DefaultTestService)
    TestService testService() {
        def mock = Mock(TestService)
        mock.doStuff() >> "mocked by " +ApplicationRunSpec.name
        return mock
    }
}
