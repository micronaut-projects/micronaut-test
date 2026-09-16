from typing import Annotated

from jakarta.inject import Inject, Singleton
from java.lang import Integer
from micronaut.context.annotation import Property, Replaces, Requires
from micronaut.http import HttpRequest
from micronaut.http.client import HttpClient
from micronaut.http.client.annotation import Client
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .MathService import MathService
from .MathServiceImpl import MathServiceImpl


@Singleton
@Requires(property="spec.name", value="MathCollaboratorTest")
@Replaces(MathServiceImpl)  # <1>
class CollaboratorMockMathService(MathService):

    def __init__(self):
        self.result = 0
        self.calls: list[int] = []

    def compute(self, num: int) -> int:
        self.calls.append(num)
        return self.result


@MicronautTest
@Property(name="spec.name", value="MathCollaboratorTest")
class MathCollaboratorTest:

    math_service: Annotated[MathService, Inject]

    client: Annotated[HttpClient, Inject, Client("/")]  # <2>

    @Test
    def test_compute_num_to_square(self):
        for num, square in [(2, 4), (3, 9)]:
            self.math_service.calls.clear()
            self.math_service.result = num * num

            result = self.client.toBlocking().retrieve(HttpRequest.GET(f"/math/compute/{num}"), Integer)  # <3>

            assert square == result
            assert [num] == self.math_service.calls  # <4>
