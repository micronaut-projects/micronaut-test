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

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.NonNull;

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
    private final String testName;
    private final boolean supportsTestMethodInterceptors;

    /**
     * @param applicationContext       The application context
     * @param testClass                The test class
     * @param testMethod               The test method
     * @param testInstance             The test instance
     * @param testException            The exception thrown by the test execution
     * @param testName                 The test name
     * @param supportsTestMethodInterceptors The indicator if the test framework supports {@link TestMethodInterceptor}.
     */
    public TestContext(
        final ApplicationContext applicationContext,
        final Class<?> testClass,
        final AnnotatedElement testMethod,
        final Object testInstance,
        final Throwable testException,
        final String testName,
        boolean supportsTestMethodInterceptors) {

        this.applicationContext = applicationContext;
        this.testClass = testClass;
        this.testException = testException;
        this.testInstance = testInstance;
        this.testMethod = testMethod;
        this.testName = testName;
        this.supportsTestMethodInterceptors = supportsTestMethodInterceptors;
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

    /**
     * @return The test name
     */
    @NonNull
    public String getTestName() {
        return testName;
    }

    /**
     * @return True if {@link TestMethodInterceptor} is supported
     */
    public boolean isSupportsTestMethodInterceptors() {
        return supportsTestMethodInterceptors;
    }
}
