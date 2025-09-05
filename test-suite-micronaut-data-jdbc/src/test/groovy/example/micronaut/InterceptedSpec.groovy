package example.micronaut


import io.micronaut.context.annotation.Property
import io.micronaut.data.connection.ConnectionOperations
import io.micronaut.data.connection.annotation.Connectable
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.PendingFeature
import spock.lang.Specification

import java.sql.Connection

@H2DBProperties
@MicronautTest(startApplication = false, transactional = false)
@Property(name = "datasources.default.allow-connection-per-operation", value = "false")
class InterceptedSpec extends Specification {

    @Inject
    ConnectionOperations<Connection> connectionOperations

    @PendingFeature(reason = "Something is strange with Spock processing")
    @Connectable
    def "there are two products"() {
        expect:
        connectionOperations.findConnectionStatus().isPresent()
    }
}
