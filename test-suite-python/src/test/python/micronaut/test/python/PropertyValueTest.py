from typing import Annotated

from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test


@MicronautTest
@Property(name="foo.bar", value="stuff")
class PropertyValueTest:

    val: Annotated[str, Property(name="foo.bar")]

    @Test
    def test_initial_value(self):
        assert "stuff" == self.val

    @Property(name="foo.bar", value="changed")
    @Test
    def test_value_changed(self):
        assert "changed" == self.val
