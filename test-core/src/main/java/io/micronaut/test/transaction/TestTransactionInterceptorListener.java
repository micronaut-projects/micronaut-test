/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
