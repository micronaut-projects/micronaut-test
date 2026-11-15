from typing import Annotated

from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from micronaut.test.support import TestPropertyProvider
from org.junit.jupiter.api import Test, TestInstance


@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PropertySourceMapTest(TestPropertyProvider):

    val: Annotated[str, Property(name="foo.bar")]

    @Test
    def test_property_source(self):
        assert "one" == self.val

    def getProperties(self) -> dict[str, str]:
        return {
            "foo.bar": "one",
            "foo.baz": "two",
        }
