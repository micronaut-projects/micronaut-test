from typing import Annotated

from jakarta.inject import Inject
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Nested, Test

from .OrderService import OrderService


@MicronautTest
class OrderServiceTest:

    order_service: Annotated[OrderService, Inject]  # <1>

    @Test
    def test_places_an_order(self):
        assert self.order_service.place("book") == "placed book"

    @Nested
    class Placing:

        order_service: Annotated[OrderService, Inject]

        @Test
        def test_places_an_order_from_the_nested_test(self):
            assert self.order_service.place("pen") == "placed pen"

    @Nested
    class Cancelling:

        order_service: Annotated[OrderService, Inject]

        @Test
        def test_cancels_an_order(self):  # <2>
            assert self.order_service.cancel("book") == "cancelled book"
