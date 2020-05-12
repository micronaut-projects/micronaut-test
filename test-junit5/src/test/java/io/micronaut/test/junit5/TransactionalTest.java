package io.micronaut.test.junit5;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.transaction.spring.SpringTransactionTestExecutionListener;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest(transactional = true)
@DbProperties
class TransactionalTest {

  @Inject
  ApplicationContext applicationContext;

  @Test
  void testSpringTransactionListenerMissing() {
    Assertions.assertTrue(applicationContext.containsBean(SpringTransactionTestExecutionListener.class));
  }

}
