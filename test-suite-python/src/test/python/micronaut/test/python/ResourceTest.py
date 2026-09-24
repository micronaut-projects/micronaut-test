import os
import tempfile

from jakarta.annotation import PostConstruct, PreDestroy
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test


@MicronautTest
class ResourceTest:

    workspace: str | None = None

    @PostConstruct
    def open(self):
        self.workspace = tempfile.mkdtemp(prefix="test")

    @PreDestroy
    def close(self):
        if self.workspace is not None:
            os.rmdir(self.workspace)

    @Test
    def test_workspace_available(self):
        assert self.workspace is not None
        assert os.path.isdir(self.workspace)
