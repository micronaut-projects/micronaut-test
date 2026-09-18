from typing import Annotated

from jakarta.inject import Inject
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test
from org.junit.jupiter.api.parallel import Execution, ExecutionMode

from .MathService import MathService


@MicronautTest
@Execution(ExecutionMode.CONCURRENT)  # <1>
class ReadOnlyEndpointTest:

    math_service: Annotated[MathService, Inject]

    @Test
    def test_two(self):
        assert 8 == self.math_service.compute(2)

    @Test
    def test_three(self):
        assert 12 == self.math_service.compute(3)
