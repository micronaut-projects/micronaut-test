package io.micronaut.test.junit5;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.transaction.TransactionOperations;
import io.micronaut.transaction.test.DefaultTestTransactionExecutionListener;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@MicronautTest(transactional = true)
@io.micronaut.context.annotation.Property(name = "datasources.default.url", value = "jdbc:h2:mem:TransactionalTest;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
@DbProperties
class TransactionalTest {

  @Inject
  ApplicationContext applicationContext;

  @Inject
  TransactionOperations<?> transactionOperations;

  @BeforeEach
  void setup() {
    Assertions.assertTrue(transactionOperations.findTransactionStatus().isPresent());
  }

  @AfterEach
  void cleanup() {
      Assertions.assertTrue(transactionOperations.findTransactionStatus().isPresent());
  }

  @Test
  void testSpringTransactionListenerMissing() {
    Assertions.assertTrue(applicationContext.containsBean(DefaultTestTransactionExecutionListener.class));
  }

}
