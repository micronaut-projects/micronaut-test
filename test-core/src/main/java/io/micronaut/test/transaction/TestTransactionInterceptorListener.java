package io.micronaut.test.transaction;

import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.test.context.TestContext;
import io.micronaut.test.context.TestExecutionListener;
import io.micronaut.test.extensions.AbstractMicronautExtension;

/**
 * Test execution listener for retro*compatibility with legacy {@link TestTransactionInterceptor}.
 *
 * @author bidorffOL
 * @since 1.2
 */
@EachBean(TestTransactionInterceptor.class)
@Requires(property = AbstractMicronautExtension.TEST_TRANSACTIONAL, value = StringUtils.TRUE, defaultValue = StringUtils.TRUE)
public class TestTransactionInterceptorListener implements TestExecutionListener {

  private final TestTransactionInterceptor interceptor;
  private final boolean rollback;

  /**
   * @param interceptor the interceptor
   * @param rollback {@code true} if the transaction should be rollback
   */
  public TestTransactionInterceptorListener(
      TestTransactionInterceptor interceptor,
      @Property(name = AbstractMicronautExtension.TEST_ROLLBACK) boolean rollback) {

    this.interceptor = interceptor;
    this.rollback = rollback;

  }

  @Override
  public void afterTestExecution(TestContext testContext) {
    if (rollback) {
      interceptor.rollback();
    } else {
      interceptor.commit();
    }
  }

  @Override
  public void beforeTestExecution(TestContext testContext) {
    interceptor.begin();
  }

}
