package io.micronaut.test.junit5;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.transaction.test.DefaultTestTransactionExecutionListener;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest(transactional = false)
@DbProperties
class NonTransactionalTest {

  @Inject
  ApplicationContext applicationContext;

  @Test
  void testSpringTransactionListenerMissing() {
    Assertions.assertFalse(applicationContext.containsBean(DefaultTestTransactionExecutionListener.class));
  }

}
