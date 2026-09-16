from typing import Annotated

import java
from jakarta.inject import Inject
from java.lang import String
from micronaut.context.annotation import Property
from micronaut.http import HttpStatus
from micronaut.http.client import HttpClient
from micronaut.http.client.annotation import Client
from micronaut.runtime.server import EmbeddedServer
from micronaut.test.extensions.junit5.annotation import MicronautTest
from micronaut.test.support.server import TestExecutableEmbeddedServer
from org.junit.jupiter.api import Test


@MicronautTest
# tag::executable[]
@Property(
    name="micronaut.test.server.executable",
    value="../test-junit5/src/test/apps/test-app.jar"
)
# end::executable[]
@Property(name="test.property", value="good")
class ProcessServerTest:

    embedded_server: Annotated[EmbeddedServer, Inject]

    client: Annotated[HttpClient, Inject, Client("/")]

    @Test
    def test_server_available(self):
        response = self.client.toBlocking().exchange("/test", String)

        assert java.instanceof(self.embedded_server, TestExecutableEmbeddedServer)
        assert self.embedded_server.isRunning()
        assert HttpStatus.OK == response.status()
        assert "Result = good" == response.body()
