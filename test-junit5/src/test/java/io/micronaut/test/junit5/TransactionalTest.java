
package io.micronaut.test.junit5;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.transaction.support.TransactionSynchronizationManager;
import io.micronaut.transaction.test.DefaultTestTransactionExecutionListener;
import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@MicronautTest(transactional = true)
@DbProperties
@Disabled("Disabled until we get a working micronaut-data for 4.0.0")
class TransactionalTest {

  @Inject
  ApplicationContext applicationContext;

  @BeforeEach
  void setup() {
    Assertions.assertTrue(TransactionSynchronizationManager.isSynchronizationActive());
  }

  @AfterEach
  void cleanup() {
    Assertions.assertTrue(TransactionSynchronizationManager.isSynchronizationActive());
  }

  @Test
  void testSpringTransactionListenerMissing() {
    Assertions.assertTrue(applicationContext.containsBean(DefaultTestTransactionExecutionListener.class));
  }

}
