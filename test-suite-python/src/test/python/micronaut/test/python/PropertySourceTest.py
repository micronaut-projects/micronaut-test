from typing import Annotated

from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test


@MicronautTest(propertySources="myprops.properties")
class PropertySourceTest:

    val: Annotated[str, Property(name="foo.bar")]

    @Test
    def test_property_source(self):
        assert "foo" == self.val


@MicronautTest(propertySources="file:src/test/resources/micronaut/test/python/fileprops.properties")
class FilePropertySourceTest:

    val: Annotated[str, Property(name="foo.file")]

    @Test
    def test_file_property_source(self):
        assert "file" == self.val
