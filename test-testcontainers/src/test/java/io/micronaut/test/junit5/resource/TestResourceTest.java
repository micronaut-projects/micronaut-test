package io.micronaut.test.junit5.resource;

import java.net.URI;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.io.socket.SocketUtils;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
public class TestResourceTest {

    @Test
    void testResourceAvailable(@Property(name = "mongodb.uri") URI uri) {
        final int port = uri.getPort();
        Assertions.assertFalse(SocketUtils.isTcpPortAvailable(port));
    }
}
