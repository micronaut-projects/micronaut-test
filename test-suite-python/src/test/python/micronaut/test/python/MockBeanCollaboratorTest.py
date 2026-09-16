from typing import Annotated

from jakarta.inject import Inject
from java.lang import Integer
from micronaut.http import HttpRequest
from micronaut.http.client import HttpClient
from micronaut.http.client.annotation import Client
from micronaut.test.annotation import MockBean
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Disabled, Test

from .MathService import MathService
from .MathServiceImpl import MathServiceImpl


class MockBeanMathService(MathService):

    def __init__(self):
        self.result = 0
        self.calls: list[int] = []

    def compute(self, num: int) -> int:
        self.calls.append(num)
        return self.result


# TODO(python): the AOP proxy generated for a @MockBean factory method (a @Refreshable @Around bean) whose type is a
# Python class calls the generated no-arg constructor, which creates an unrelated Python instance, and its
# asPolyglotValue() is not intercepted: every Python consumer of the proxy (this test, MathController) receives that
# fresh instance instead of the mock returned by the factory method. Use @Replaces (MathCollaboratorTest) instead.
@Disabled("TODO(python): @MockBean factory methods returning a Python class are not proxied correctly")
@MicronautTest
class MockBeanCollaboratorTest:

    math_service: Annotated[MathService, Inject]

    client: Annotated[HttpClient, Inject, Client("/")]

    @Test
    def test_compute_num_to_square(self):
        for num, square in [(2, 4), (3, 9)]:
            self.math_service.result = num * num

            result = self.client.toBlocking().retrieve(HttpRequest.GET(f"/math/compute/{num}"), Integer)

            assert square == result
            assert num in self.math_service.calls

    @MockBean(MathServiceImpl)
    def math_service_mock(self) -> MockBeanMathService:
        return MockBeanMathService()
