from typing import Annotated

from jakarta.inject import Inject, Singleton
from micronaut.context.annotation import Property, Replaces, Requires
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .MathService import MathService
from .MathServiceImpl import MathServiceImpl


@Singleton
@Requires(property="spec.name", value="MathMockServiceTest")  # <1>
@Replaces(MathServiceImpl)  # <2>
class MockMathService(MathService):

    def __init__(self):
        self.result = 0
        self.calls: list[int] = []

    def compute(self, num: int) -> int:
        self.calls.append(num)
        return self.result


@MicronautTest
@Property(name="spec.name", value="MathMockServiceTest")  # <3>
class MathMockServiceTest:

    math_service: Annotated[MathService, Inject]  # <4>

    @Test
    def test_compute_num_to_square(self):
        for num, square in [(2, 4), (3, 9)]:
            self.math_service.calls.clear()
            self.math_service.result = num * num

            result = self.math_service.compute(10)

            assert square == result
            assert [10] == self.math_service.calls  # <5>
