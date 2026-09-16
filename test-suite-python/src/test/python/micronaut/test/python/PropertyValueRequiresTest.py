from abc import ABC
from typing import Annotated

from jakarta.inject import Inject, Singleton
from micronaut.context.annotation import Property, Requires
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test


class MyService(ABC):
    pass


@Singleton
@Requires(property="foo.bar", value="stuff")
class MyServiceStuff(MyService):
    pass


@Singleton
@Requires(property="foo.bar", value="changed")
class MyServiceChanged(MyService):
    pass


# https://github.com/micronaut-projects/micronaut-test/issues/91
@MicronautTest(rebuildContext=True)
@Property(name="foo.bar", value="stuff")
class PropertyValueRequiresTest:

    my_service: Annotated[MyService, Inject]

    @Test
    def test_initial_value(self):
        assert isinstance(self.my_service, MyServiceStuff)

    @Property(name="foo.bar", value="changed")
    @Test
    def test_value_changed(self):
        assert isinstance(self.my_service, MyServiceChanged)
