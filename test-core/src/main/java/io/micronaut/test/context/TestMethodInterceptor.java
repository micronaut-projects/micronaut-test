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
 * Test method interceptor. Intended to be used for API that doesn't support before/after phases.
 *
 * @param <R> The result type
 * @author Denis Stepanov
 * @since 3.4.0
 */
public interface TestMethodInterceptor<R> {

    /**
     * Intercepts before each test invocation.
     *
     * @param methodInvocationContext the method invocation context
     * @return intercepted return value
     * @throws Exception allows any exception to propagate
     */
    default R interceptBeforeEach(TestMethodInvocationContext<R> methodInvocationContext) throws Throwable {
        return methodInvocationContext.proceed();
    }

    /**
     * Intercepts each test invocation.
     *
     * @param methodInvocationContext the method invocation context
     * @return intercepted return value
     * @throws Exception allows any exception to propagate
     */
    default R interceptTest(TestMethodInvocationContext<R> methodInvocationContext) throws Throwable {
        return methodInvocationContext.proceed();
    }

    /**
     * Intercepts after each test invocation.
     *
     * @param methodInvocationContext the method invocation context
     * @return intercepted return value
     * @throws Exception allows any exception to propagate
     */
    default R interceptAfterEach(TestMethodInvocationContext<R> methodInvocationContext) throws Throwable {
        return methodInvocationContext.proceed();
    }

}
