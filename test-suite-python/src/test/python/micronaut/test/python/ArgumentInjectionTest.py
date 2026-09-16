from typing import Annotated

from micronaut.context.annotation import Property, Value
from micronaut.http.client import HttpClient
from micronaut.http.client.annotation import Client
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .MathService import MathService


@MicronautTest
@Property(name="foo.bar", value="test")
class ArgumentInjectionTest:

    @Test
    def test_argument_injected(
        self,
        math_service: MathService,
        val: Annotated[str, Property(name="foo.bar")],
        client: Annotated[HttpClient, Client("/")],
    ):
        result = math_service.compute(2)

        assert 8 == result
        assert client is not None
        assert "test" == val

    @Test
    def test_value_argument_injected(self, val: Annotated[str, Value("${foo.bar}")]):
        assert "test" == val
