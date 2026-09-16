from abc import ABC, abstractmethod


class MathService(ABC):

    @abstractmethod
    def compute(self, num: int) -> int:
        ...
