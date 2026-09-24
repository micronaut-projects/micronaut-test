from jakarta.inject import Singleton
from micronaut.context.annotation import Requires
from micronaut.test.context import TestContext, TestExecutionListener


@Requires(property="spec.name", value="TestOutcomeListenerTest")
@Singleton
class OutcomeListener(TestExecutionListener):

    def __init__(self):
        self.successful: list[str] = []
        self.failed: list[str] = []
        self.aborted: list[str] = []
        self.disabled: list[str] = []

    def testSuccessful(self, test_context: TestContext) -> None:
        self.successful.append(test_context.getTestName())

    def testFailed(self, test_context: TestContext) -> None:  # <1>
        self.failed.append(f"{test_context.getTestName()}: {test_context.getTestException()}")

    def testAborted(self, test_context: TestContext) -> None:  # <2>
        self.aborted.append(test_context.getTestName())

    def testDisabled(self, test_context: TestContext, reason: str | None) -> None:  # <3>
        self.disabled.append(f"{test_context.getTestName()}: {reason}")
