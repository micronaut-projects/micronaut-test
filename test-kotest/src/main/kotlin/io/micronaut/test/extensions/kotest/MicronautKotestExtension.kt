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
package io.micronaut.test.extensions.kotest

import io.kotest.core.extensions.ConstructorExtension
import io.kotest.core.extensions.TestCaseExtension
import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.micronaut.aop.InterceptedProxy
import io.micronaut.test.annotation.AnnotationUtils
import io.micronaut.test.annotation.MicronautTestValue
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

object MicronautKotestExtension: TestListener, ConstructorExtension, TestCaseExtension {

    override suspend fun intercept(
        testCase: TestCase,
        execute: suspend (TestCase) -> TestResult
    ): TestResult {
        val context = contexts[testCase.spec.javaClass.name]
        return if(context != null && context.getSpecDefinition() == null) {
            // It's a MicronautTest test where the bean doesn't exist
            TestResult.Ignored
        } else {
            // Not a MicronautTest test or the bean exists
            execute(testCase)
        }
    }

    val contexts: MutableMap<String, MicronautKotestContext> = mutableMapOf()

    override suspend fun beforeSpec(spec: Spec) {
        contexts[spec.javaClass.name]?.beforeSpecClass(spec)
    }

    override suspend fun afterSpec(spec: Spec) {
        contexts[spec.javaClass.name]?.afterSpecClass(spec)
    }

    override suspend fun beforeTest(testCase: TestCase) {
        contexts[testCase.spec.javaClass.name]?.beforeTest(testCase)
    }

    override suspend fun afterTest(testCase: TestCase, result: TestResult) {
        contexts[testCase.spec.javaClass.name]?.afterTest(testCase, result)
    }

    override suspend fun beforeInvocation(testCase: TestCase, iteration: Int) {
        contexts[testCase.spec.javaClass.name]?.beforeInvocation(testCase)
    }

    override suspend fun afterInvocation(testCase: TestCase, iteration: Int) {
        contexts[testCase.spec.javaClass.name]?.afterInvocation(testCase)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Spec> instantiate(clazz: KClass<T>): Spec? {
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
            val context = MicronautKotestContext(testClass, micronautTestValue, createBean)
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
typealias MicornautKotlinTestExtension = MicronautKotestExtension
