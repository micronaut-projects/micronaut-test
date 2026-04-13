package io.micronaut.test.spock

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

@MicronautTest
@Issue("https://github.com/micronaut-projects/micronaut-test/issues/1191")
@Property(name = "micronaut.http.client.ssl.insecure-trust-all-certificates", value = "true")
class CleanupSpecHttpsRequestSpec extends Specification {

    @Shared
    private static final EmbeddedServer EXTERNAL_SERVER = ApplicationContext.run(
        EmbeddedServer,
        [
            "micronaut.server.port": -1,
            "micronaut.server.ssl.enabled": true,
            "micronaut.server.ssl.build-self-signed": true
        ]
    )

    @Inject
    @Shared
    @Client("/")
    HttpClient client

    void cleanupSpec() {
        try {
            assert client.toBlocking().retrieve(HttpRequest.GET("${EXTERNAL_SERVER.getURL()}/test"), String) == "orignal"
        } finally {
            EXTERNAL_SERVER.stop()
        }
    }

    void "shared client remains usable during cleanupSpec"() {
        expect:
        client.toBlocking().retrieve(HttpRequest.GET("${EXTERNAL_SERVER.getURL()}/test"), String) == "orignal"
    }
}
