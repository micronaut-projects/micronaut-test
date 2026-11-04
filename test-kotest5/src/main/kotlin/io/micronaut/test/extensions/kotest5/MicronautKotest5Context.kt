/*
 * Copyright 2017-2023 original authors
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
package io.micronaut.test.extensions.kotest5

import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import io.micronaut.context.annotation.Property
import io.micronaut.core.propagation.PropagatedContext
import io.micronaut.test.annotation.MicronautTestValue
import io.micronaut.test.context.TestContext
import io.micronaut.test.context.TestMethodInvocationContext
import io.micronaut.test.extensions.AbstractMicronautExtension
import io.micronaut.test.support.TestPropertyProvider
import kotlinx.coroutines.ThreadContextElement
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.reflect.full.memberFunctions

class MicronautKotest5Context @JvmOverloads constructor(
    private val specClass: Class<Any>,
    private val micronautTestValue: MicronautTestValue,
    private val createBean: Boolean,
    private val sharesPermit: Boolean = false
) : AbstractMicronautExtension<Spec>() {

    override fun sharesContextParallelismPermit() = sharesPermit

    override fun resolveTestProperties(context: Spec?, testAnnotationValue: MicronautTestValue, testProperties: MutableMap<String, Any>?) {
        if (context is TestPropertyProvider) {
            testProperties?.putAll(context.properties)
        }
    }

    val bean: Spec? = if (createBean) {
        beforeClass(null, specClass, micronautTestValue)
        try {
            applicationContext.findBean(specClass).orElse(null) as Spec?
        } catch (e: Throwable) {
            // this context never reaches Kotest, so nothing else would stop it and release its permit
            stopAfterFailure(null, e)
            throw e
        }
    } else {
        null
    }

    override fun alignMocks(context: Spec?, instance: Any) {
    }

    fun isApplicationContextOpen(): Boolean {
        return applicationContext != null
    }

    fun beforeSpecClass(spec: Spec) {
        try {
            if (!createBean) {
                beforeClass(spec, specClass, micronautTestValue)
                applicationContext.inject(spec)
            }
            beforeTestClass(buildContext(spec))
        } catch (e: Throwable) {
            // Kotest does not call afterSpec after a failing beforeSpec
            stopAfterFailure(spec, e)
            throw e
        }
    }

    private fun stopAfterFailure(spec: Spec?, failure: Throwable) {
        try {
            afterClass(spec)
        } catch (e: Throwable) {
            failure.addSuppressed(e)
        }
    }

    /**
     * Stops the context and releases its parallelism permit, for a spec that Kotest instantiated
     * but never ran, in which case it does not call afterSpec.
     */
    fun stopIfRunning() {
        afterClass(null)
    }

    fun afterSpecClass(spec: Spec) {
        // A failing listener or @Sql script must not keep the context running or its parallelism permit held
        AutoCloseable { afterClass(spec) }.use {
            afterTestClass(buildContext(spec))
        }
    }

    fun beforeTest(testCase: TestCase) {
        val filter = testCase.spec::class.memberFunctions.filter { it.name == testCase.name.name }
        var propertyAnnotations: List<Property>? = emptyList()
        if (filter.isNotEmpty()) {
            propertyAnnotations = filter.first().annotations.filterIsInstance<Property>()
        }
        beforeEach(testCase.spec, testCase.spec, testCase.test.javaClass, propertyAnnotations)
        beforeTestMethod(buildContext(testCase, null))
    }

    fun afterTest(testCase: TestCase, result: TestResult) {
        afterTestMethod(buildContext(testCase, result))
    }

    fun beforeInvocation(testCase: TestCase) {
        beforeTestExecution(buildInterceptContext(testCase))
    }

    fun afterInvocation(testCase: TestCase) {
        afterTestExecution(buildInterceptContext(testCase))
    }

    suspend fun interceptTestCase(
        testCase: TestCase,
        execute: suspend (TestCase) -> TestResult
    ): TestResult {
        val currentContext = coroutineContext
        return interceptTest(object : TestMethodInvocationContext<Any> {
            override fun getTestContext(): TestContext {
                return buildInterceptContext(testCase)
            }

            override fun proceed(): Any {
                val propagatedContext = PropagatedContext.find().orElse(null)
                return if (propagatedContext == null) {
                    runBlocking(currentContext) {
                        execute(testCase)
                    }
                } else {
                    runBlocking(currentContext + CoroutinePropagatedContext(propagatedContext)) {
                        execute(testCase)
                    }
                }
            }
        }) as TestResult
    }

    fun getSpecDefinition() = specDefinition

    /**
     * Builds a TestContext for spec-level lifecycle callbacks (beforeSpecClass, afterSpecClass).
     * Sets supportsTestMethodInterceptors = false because these callbacks occur outside
     * the test method interception chain and are used for Micronaut's beforeTestClass/afterTestClass.
     */
    fun buildContext(spec: Spec): TestContext {
        return TestContext(
            applicationContext,
            spec.javaClass,
            null,
            spec,
            null,
            spec.javaClass.simpleName,
            false
        )
    }

    /**
     * Builds a TestContext for test-level lifecycle callbacks (beforeTest, afterTest).
     * Sets supportsTestMethodInterceptors = false because these callbacks occur outside
     * the test method interception chain and are used for Micronaut's beforeTestMethod/afterTestMethod.
     */
    fun buildContext(testCase: TestCase, result: TestResult?): TestContext {
        val error = when (result) {
            is TestResult.Error -> result.cause
            is TestResult.Ignored,
            is TestResult.Success,
            is TestResult.Failure,
            null -> null
        }

        return TestContext(
            applicationContext,
            testCase.spec.javaClass,
            testCase.test.javaClass,
            testCase.spec,
            error,
            testCase.name.name,
            false
        )
    }

    /**
     * Builds a TestContext for test execution interception (beforeTestExecution, afterTestExecution).
     * Sets supportsTestMethodInterceptors = true because this context is used within
     * the test method interception chain (interceptTest).
     */
    private fun buildInterceptContext(testCase: TestCase): TestContext {
        return TestContext(
            applicationContext,
            testCase.spec.javaClass,
            testCase.test.javaClass,
            testCase.spec,
            null,
            testCase.name.name,
            true
        )
    }

    private class CoroutinePropagatedContext(
        private val propagatedContext: PropagatedContext
    ) : ThreadContextElement<PropagatedContext.Scope>, AbstractCoroutineContextElement(Key) {

        companion object Key : CoroutineContext.Key<CoroutinePropagatedContext>

        @Suppress("DEPRECATION")
        override fun updateThreadContext(context: CoroutineContext): PropagatedContext.Scope {
            return propagatedContext.propagate()
        }

        override fun restoreThreadContext(context: CoroutineContext, oldState: PropagatedContext.Scope) {
            oldState.close()
        }
    }
}
