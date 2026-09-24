from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from micronaut.transaction import TransactionOperations
from org.junit.jupiter.api import AfterEach, BeforeEach, Test


@MicronautTest(transactional=True)  # <1>
@Property(name="datasources.default.url", value="jdbc:h2:mem:TransactionalTest;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
class TransactionalTest:

    transaction_operations: Annotated[TransactionOperations, Inject]

    @BeforeEach
    def setup(self):
        assert self.transaction_operations.findTransactionStatus().isPresent()  # <2>

    @AfterEach
    def cleanup(self):
        assert self.transaction_operations.findTransactionStatus().isPresent()  # <3>

    @Test
    def test_runs_inside_a_transaction(self):
        assert self.transaction_operations.findTransactionStatus().isPresent()  # <4>
