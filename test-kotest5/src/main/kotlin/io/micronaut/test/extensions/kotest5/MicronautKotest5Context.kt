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
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.full.memberFunctions

class MicronautKotest5Context(
    private val specClass: Class<Any>,
    private val micronautTestValue: MicronautTestValue,
    private val createBean: Boolean
) : AbstractMicronautExtension<Spec>() {

    override fun resolveTestProperties(context: Spec?, testAnnotationValue: MicronautTestValue, testProperties: MutableMap<String, Any>?) {
        if (context is TestPropertyProvider) {
            testProperties?.putAll(context.properties)
        }
    }

    val bean: Spec? = if (createBean) {
        beforeClass(null, specClass, micronautTestValue)
        applicationContext.findBean(specClass).orElse(null) as Spec?
    } else {
        null
    }

    override fun alignMocks(context: Spec?, instance: Any) {
    }

    fun isApplicationContextOpen(): Boolean {
        return applicationContext != null
    }

    fun beforeSpecClass(spec: Spec) {
        if (!createBean) {
            beforeClass(spec, specClass, micronautTestValue)
            applicationContext.inject(spec)
        }
        beforeTestClass(buildContext(spec))
    }

    fun afterSpecClass(spec: Spec) {
        afterTestClass(buildContext(spec))
        afterClass(spec)
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
        val kotestCoroutineContext = currentCoroutineContext()
        return interceptTest(object : TestMethodInvocationContext<Any> {
            override fun getTestContext(): TestContext {
                return buildInterceptContext(testCase)
            }

            override fun proceed(): Any {
                val propagatedContext = PropagatedContext.find().orElse(null)
                return if (propagatedContext == null) {
                    runBlocking(kotestCoroutineContext) {
                        execute(testCase)
                    }
                } else {
                    runBlocking(kotestCoroutineContext + CoroutinePropagatedContext(propagatedContext)) {
                        execute(testCase)
                    }
                }
            }
        }) as TestResult
    }

    fun getSpecDefinition() = specDefinition

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
