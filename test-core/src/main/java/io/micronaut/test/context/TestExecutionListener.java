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
package io.micronaut.test.context;

/**
 * Test execution listener.
 *
 * @author bidorffOL
 * @since 1.2
 */
public interface TestExecutionListener {

  /**
   * Executed before all of the tests of a class are executed.
   *
   * @param testContext the test context
   * @throws Exception allows any exception to propagate
   */
  default void beforeTestClass(TestContext testContext) throws Exception {

  }

  /**
   * Executed before the setup method of a test method.
   *
   * @param testContext the test context
   * @throws Exception allows any exception to propagate
   */
  default void beforeSetupTest(TestContext testContext) throws Exception {

  }

  /**
   * Executed after the setup method of a test method.
   *
   * @param testContext the test context
   * @throws Exception allows any exception to propagate
   */
  default void afterSetupTest(TestContext testContext) throws Exception {

  }

  /**
   * Executed before the setup method of a test method.
   *
   * @param testContext the test context
   * @throws Exception allows any exception to propagate
   */
  default void beforeCleanupTest(TestContext testContext) throws Exception {

  }

  /**
   * Executed after the setup method of a test method.
   *
   * @param testContext the test context
   * @throws Exception allows any exception to propagate
   */
  default void afterCleanupTest(TestContext testContext) throws Exception {

  }

  /**
   * Executed before a test method is executed (a test method may contain multiple iterations).
   *
   * @param testContext the test context
   * @throws Exception allows any exception to propagate
   */
  default void beforeTestMethod(TestContext testContext) throws Exception {

  }

  /**
   * Executed before a single test iteration is executed.
   *
   * @param testContext the test context
   * @throws Exception allows any exception to propagate
   */
  default void beforeTestExecution(TestContext testContext) throws Exception {

  }

  /**
   * Executed after a single test iteration has been executed .
   *
   * @param testContext the test context
   * @throws Exception allows any exception to propagate
   */
  default void afterTestExecution(TestContext testContext) throws Exception {

  }

  /**
   * Executed after a test method has been executed (a test method may contain multiple
   * iterations).
   *
   * @param testContext the test context
   * @throws Exception allows any exception to propagate
   */
  default void afterTestMethod(TestContext testContext) throws Exception {

  }

  /**
   * Executed after all of the tests of a class have bean executed.
   *
   * @param testContext the test context
   * @throws Exception allows any exception to propagate
   */
  default void afterTestClass(TestContext testContext) throws Exception {

  }

}
