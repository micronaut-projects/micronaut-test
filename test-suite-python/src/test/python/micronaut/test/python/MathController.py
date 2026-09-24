from micronaut.http import MediaType
from micronaut.http.annotation import Controller, Get

from .MathService import MathService


@Controller("/math")
class MathController:

    def __init__(self, math_service: MathService):
        self.math_service = math_service

    @Get(uri="/compute/{number}", processes=MediaType.TEXT_PLAIN)
    def compute(self, number: int) -> str:
        return str(self.math_service.compute(number))
