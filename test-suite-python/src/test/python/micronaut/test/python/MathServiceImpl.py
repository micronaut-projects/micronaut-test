from jakarta.inject import Singleton

from .MathService import MathService


@Singleton
class MathServiceImpl(MathService):

    def compute(self, num: int) -> int:
        return num * 4
