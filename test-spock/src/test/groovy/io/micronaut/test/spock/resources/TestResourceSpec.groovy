package io.micronaut.test.spock.resources

import io.micronaut.context.annotation.Property
import io.micronaut.core.io.socket.SocketUtils
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

@MicronautTest
class TestResourceSpec extends Specification {
    @Property(name="mongodb.uri")
    URI uri

    void "test test resource is available"() {
        expect:
        !SocketUtils.isTcpPortAvailable(uri.getPort())
    }
}
