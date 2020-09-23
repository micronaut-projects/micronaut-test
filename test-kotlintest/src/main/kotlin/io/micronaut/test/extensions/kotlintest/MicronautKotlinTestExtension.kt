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
package io.micronaut.test.extensions.kotlintest

import io.kotlintest.Spec
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.extensions.ConstructorExtension
import io.kotlintest.extensions.TestCaseExtension
import io.kotlintest.extensions.TestListener
import io.kotlintest.extensions.TopLevelTest
import io.micronaut.aop.InterceptedProxy
import io.micronaut.test.annotation.AnnotationUtils
import io.micronaut.test.annotation.MicronautTestValue
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

@Deprecated(
    "KotlinTest is now deprecated in favor of Kotest (KotlinTest's 4.0.0 version). Upgrade to Kotest is recommended."
)
object MicronautKotlinTestExtension: TestListener, ConstructorExtension, TestCaseExtension {


    override suspend fun intercept(testCase: TestCase,
                                   execute: suspend (TestCase, suspend (TestResult) -> Unit) -> Unit,
                                   complete: suspend (TestResult) -> Unit) {
        val context = contexts[testCase.spec.javaClass.name]
        if (context != null && context.getSpecDefinition() == null) {
            //Its a MicronautTest test where the bean doesn't exist
            complete(TestResult.Ignored)
        } else {
            //Not a MicronautTest test or the bean exists
            execute(testCase, complete)
        }
    }

    val contexts: MutableMap<String, MicronautKotlinTestContext> = mutableMapOf()

    override fun beforeSpecClass(spec: Spec, tests: List<TopLevelTest>) {
        contexts[spec.javaClass.name]?.beforeSpecClass(spec)
    }

    override fun afterSpecClass(spec: Spec, results: Map<TestCase, TestResult>) {
        contexts[spec.javaClass.name]?.afterSpecClass(spec)
    }

    override fun beforeTest(testCase: TestCase) {
        contexts[testCase.spec.javaClass.name]?.beforeTest(testCase)
        contexts[testCase.spec.javaClass.name]?.beforeInvocation(testCase)
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        contexts[testCase.spec.javaClass.name]?.afterInvocation(testCase)
        contexts[testCase.spec.javaClass.name]?.afterTest(testCase, result)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Spec> instantiate(clazz: KClass<T>): Spec? {
        // we only instantiate via spring if there's actually parameters in the constructor
        // otherwise there's nothing to inject there
        val constructor = clazz.primaryConstructor
        val testClass: Class<Any> = clazz.java as Class<Any>
        var micronautTestValue = testClass
                .annotations
                .filterIsInstance<io.micronaut.test.annotation.MicronautTest>()
                .map { micronautTest -> AnnotationUtils.buildValueObject(micronautTest) }
                .firstOrNull()
        if (micronautTestValue == null) {
            micronautTestValue = testClass
                    .annotations
                    .filterIsInstance<MicronautTest>()
                    .map { micronautTest -> buildValueObject(micronautTest) }
                    .firstOrNull()
        }

        return if (micronautTestValue == null) {
            null
        } else {
            val createBean = constructor != null && constructor.parameters.isNotEmpty()
            val context = MicronautKotlinTestContext(testClass, micronautTestValue, createBean)
            contexts[testClass.name] = context
            if (createBean) {
                context.bean
            } else {
                null
            }
        }
    }

    private fun buildValueObject(micronautTest: MicronautTest): MicronautTestValue {
        return MicronautTestValue(
                micronautTest.application.java,
                micronautTest.environments,
                micronautTest.packages,
                micronautTest.propertySources,
                micronautTest.rollback,
                micronautTest.transactional,
                micronautTest.rebuildContext,
                micronautTest.contextBuilder.map { kClass -> kClass.java }.toTypedArray(),
                micronautTest.transactionMode
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> Spec.getMock(obj: T): T {
        return if (obj is InterceptedProxy<*>) {
            obj.interceptedTarget() as T
        } else {
            obj
        }
    }
}

@Deprecated(message = "MicornautKotlinTestExtension is deprecated", replaceWith = ReplaceWith("MicronautKotlinTestExtension"))
typealias MicornautKotlinTestExtension = MicronautKotlinTestExtension
