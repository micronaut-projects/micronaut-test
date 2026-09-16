from jakarta.inject import Singleton
from micronaut.context.annotation import Executable, Requires
from micronaut.test.context import TestContext, TestExecutionListener


@Requires(property="spec.name", value="TestOutcomeListenerTest")
@Singleton
class OutcomeListener(TestExecutionListener):

    def __init__(self):
        self.successful: list[str] = []
        self.failed: list[str] = []
        self.aborted: list[str] = []
        self.disabled: list[str] = []

    @Executable
    def testSuccessful(self, test_context: TestContext) -> None:
        self.successful.append(test_context.getTestName())

    @Executable
    def testFailed(self, test_context: TestContext) -> None:  # <1>
        self.failed.append(f"{test_context.getTestName()}: {test_context.getTestException()}")

    @Executable
    def testAborted(self, test_context: TestContext) -> None:  # <2>
        self.aborted.append(test_context.getTestName())

    @Executable
    def testDisabled(self, test_context: TestContext, reason: str | None) -> None:  # <3>
        self.disabled.append(f"{test_context.getTestName()}: {reason}")
