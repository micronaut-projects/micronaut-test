from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from micronaut.transaction import TransactionOperations
from org.junit.jupiter.api import Test


@MicronautTest(transactional=False)  # <1>
@Property(name="datasources.default.url", value="jdbc:h2:mem:NonTransactionalTest;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
class NonTransactionalTest:

    transaction_operations: Annotated[TransactionOperations, Inject]

    @Test
    def test_runs_without_a_transaction(self):
        assert self.transaction_operations.findTransactionStatus().isEmpty()  # <2>
