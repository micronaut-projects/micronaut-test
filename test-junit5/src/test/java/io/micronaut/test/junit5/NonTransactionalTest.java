package io.micronaut.test.junit5;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.transaction.test.DefaultTestTransactionExecutionListener;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest(transactional = false)
@io.micronaut.context.annotation.Property(name = "datasources.default.url", value = "jdbc:h2:mem:NonTransactionalTest;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
@DbProperties
class NonTransactionalTest {

  @Inject
  ApplicationContext applicationContext;

  @Test
  void testSpringTransactionListenerMissing() {
    Assertions.assertFalse(applicationContext.containsBean(DefaultTestTransactionExecutionListener.class));
  }

}
