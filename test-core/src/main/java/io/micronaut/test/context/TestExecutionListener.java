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
