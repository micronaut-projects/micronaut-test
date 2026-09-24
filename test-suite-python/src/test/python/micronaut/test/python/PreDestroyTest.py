from jakarta.annotation import PostConstruct, PreDestroy
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import MethodOrderer, Order, Test, TestMethodOrder

# Module-level state survives the test instances, which are created per test method
events: list[str] = []


# Verifies the "Test instance lifecycle" section of the Python chapter: @PostConstruct and @PreDestroy are
# called once per test instance, i.e. once per test method under the default PER_METHOD lifecycle.
@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation)
class PreDestroyTest:

    @PostConstruct
    def open(self):
        events.append("open")

    @PreDestroy
    def close(self):
        events.append("close")

    @Test
    @Order(1)
    def test_first_instance(self):
        assert ["open"] == events

    @Test
    @Order(2)
    def test_second_instance(self):
        assert ["open", "close", "open"] == events
