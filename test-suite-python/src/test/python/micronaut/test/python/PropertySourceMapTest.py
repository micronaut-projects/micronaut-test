from typing import Annotated

from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from micronaut.test.support import TestPropertyProvider
from org.junit.jupiter.api import Disabled, Test, TestInstance


# TODO(python): TestPropertyProvider.getProperties() is called before the Micronaut context (and with it the
# GraalPy runtime) exists, so a Python test cannot provide properties this way ("GraalPy context has not been initialized").
@Disabled("TODO(python): TestPropertyProvider needs the GraalPy runtime before the context starts")
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
