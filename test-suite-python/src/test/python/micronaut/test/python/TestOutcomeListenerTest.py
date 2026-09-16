from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Disabled, MethodOrderer, Order, Test, TestMethodOrder

from .OutcomeListener import OutcomeListener


@MicronautTest
@Property(name="spec.name", value="TestOutcomeListenerTest")
@TestMethodOrder(value=MethodOrderer.OrderAnnotation)
class TestOutcomeListenerTest:

    listener: Annotated[OutcomeListener, Inject]

    @Test
    @Order(1)
    def test_first(self):
        assert self.listener is not None

    @Test
    @Disabled("never runs")
    def test_disabled(self):
        assert False

    @Test
    @Order(2)
    def test_outcomes_are_reported(self):
        assert "test_first()" in self.listener.successful
        assert self.listener.failed == []
