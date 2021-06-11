
package io.micronaut.test.junit5;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.transaction.spring.SpringTransactionTestExecutionListener;
import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@MicronautTest(transactional = true)
@DbProperties
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
    Assertions.assertTrue(applicationContext.containsBean(SpringTransactionTestExecutionListener.class));
  }

}
