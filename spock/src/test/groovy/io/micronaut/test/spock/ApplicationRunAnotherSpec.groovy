package io.micronaut.test.spock

import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.spock.annotation.MicronautTest
import io.micronaut.test.spock.annotation.MockBean
import spock.lang.Specification

import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest
class ApplicationRunAnotherSpec extends Specification {

    @Inject
    @Client("/")
    RxHttpClient client

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

