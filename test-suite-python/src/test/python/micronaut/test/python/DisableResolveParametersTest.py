from jakarta.inject import Singleton
from micronaut.context.annotation import Property, Requires
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Disabled, Test


@Requires(property="spec.name", value="DisableResolveParametersTest")
@Singleton
class Foo:
    pass


@Property(name="spec.name", value="DisableResolveParametersTest")
@MicronautTest(resolveParameters=False)
class DisableResolveParametersTest:

    @Test
    def foo(self):  # <1>
        pass

    @Test
    @Disabled("Doesn't work with resolveParameters set to False")  # <2>
    def bar(self, arg: Foo):
        assert arg is not None
