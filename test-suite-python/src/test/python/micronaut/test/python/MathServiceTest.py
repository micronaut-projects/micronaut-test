from typing import Annotated

from jakarta.inject import Inject
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .MathService import MathService


@MicronautTest  # <1>
class MathServiceTest:

    math_service: Annotated[MathService, Inject]  # <2>

    @Test
    def test_compute_num_to_square(self):
        for num, square in [(2, 8), (3, 12)]:
            result = self.math_service.compute(num)  # <3>

            assert square == result
