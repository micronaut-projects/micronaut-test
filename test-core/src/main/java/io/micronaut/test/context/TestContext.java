package io.micronaut.test.context;

import io.micronaut.context.ApplicationContext;
import java.lang.reflect.AnnotatedElement;

/**
 * Test context used by {@link TestExecutionListener}s.
 *
 * @author bidorffOL
 * @since 1.2
 */
public class TestContext {

  private final ApplicationContext applicationContext;
  private final Class<?> testClass;
  private final AnnotatedElement testMethod;
  private final Throwable testException;
  private final Object testInstance;

  /**
   * @param applicationContext The application context
   * @param testClass The test class
   * @param testMethod The test method
   * @param testInstance The test instance
   * @param testException The exception thrown by the test execution
   */
  public TestContext(
      final ApplicationContext applicationContext,
      final Class<?> testClass,
      final AnnotatedElement testMethod,
      final Object testInstance,
      final Throwable testException) {

    this.applicationContext = applicationContext;
    this.testClass = testClass;
    this.testException = testException;
    this.testInstance = testInstance;
    this.testMethod = testMethod;

  }

  /**
   * @return The application context
   */
  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  /**
   * @return The test class
   */
  public Class<?> getTestClass() {
    return testClass;
  }

  /**
   * @return The test instance
   */
  public Throwable getTestException() {
    return testException;
  }

  /**
   * @return The test method
   */
  public AnnotatedElement getTestMethod() {
    return testMethod;
  }

  /**
   * @return The exception thrown by the test execution
   */
  public Object getTestInstance() {
    return testInstance;
  }

}
