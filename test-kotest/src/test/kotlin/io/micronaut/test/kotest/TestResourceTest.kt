package io.micronaut.test.kotest

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Property
import io.micronaut.core.io.socket.SocketUtils
import io.micronaut.test.extensions.kotest.annotation.MicronautTest
import java.net.URI

@MicronautTest
class TestResourceTest(
    @Property(name="mongodb.uri") uri : URI
) : BehaviorSpec ({
    given("the a test resource is available service") {

        `when`("the port is checked for availability") {
            val tcpPortAvailable = SocketUtils.isTcpPortAvailable(uri.port)
            then("the service should be runnning") {
                tcpPortAvailable shouldBe false
            }
        }
    }
})