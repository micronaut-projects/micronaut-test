from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.env import Environment
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test


@MicronautTest(environments=["foo", "bar"])
class EnvironmentsTest:

    environment: Annotated[Environment, Inject]

    @Test
    def test_environments_are_active(self):
        active = self.environment.getActiveNames()
        assert active.contains("foo")
        assert active.contains("bar")
