package io.micronaut.test.spock

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import spock.lang.Specification

import jakarta.inject.Inject

@MicronautTest
class ApplicationRunAnotherSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient client

    void "test ping server"() {
        expect:
        client.toBlocking().retrieve(HttpRequest.GET('/test'), String) == "mocked by " +ApplicationRunAnotherSpec.name
    }

    void "test ping server again"() {
        expect:
        client.toBlocking().retrieve(HttpRequest.GET('/test'), String) == "mocked by " +ApplicationRunAnotherSpec.name
    }

    @MockBean(DefaultTestService)
    TestService testService() {
        def mock = Mock(TestService)
        mock.doStuff() >> "mocked by " +ApplicationRunAnotherSpec.name
        return mock
    }
}

