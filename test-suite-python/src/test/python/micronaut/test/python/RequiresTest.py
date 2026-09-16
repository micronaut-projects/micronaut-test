from micronaut.context.annotation import Requires
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test


@MicronautTest
@Requires(property="does.not.exist")
class RequiresTest:

    @Test
    def test_not_executed(self):
        assert False, "Should never be executed"
