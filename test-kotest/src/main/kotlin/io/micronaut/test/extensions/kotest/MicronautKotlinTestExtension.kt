/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotlintest.Spec
import io.micronaut.aop.InterceptedProxy
import io.micronaut.test.annotation.MicronautTest
import org.junit.platform.commons.support.AnnotationSupport
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

object MicronautKotlinTestExtension: TestListener, ConstructorExtension, TestCaseExtension {
    val contexts: MutableMap<String, MicronautKotlinTestContext> = mutableMapOf()

    override suspend fun intercept(testCase: TestCase, execute: suspend (TestCase) -> TestResult): TestResult {
        val context = contexts[testCase.spec.javaClass.name]
        return if (context != null && context.getSpecDefinition() == null) {
            //It's a MicronautTest test where the bean doesn't exist
            TestResult.Ignored
        } else {
            //Not a MicronautTest test, or the bean exists
            execute(testCase)
        }
    }


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
        contexts[testCase.spec.javaClass.name]?.afterTest(testCase)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Spec> instantiate(clazz: KClass<T>): Spec? {
        // we only instantiate via spring if there's actually parameters in the constructor
        // otherwise, there's nothing to inject there
        val constructor = clazz.primaryConstructor
        val testClass: Class<Any> = clazz.java as Class<Any>
        val micronautTest = AnnotationSupport.findAnnotation<MicronautTest>(testClass, MicronautTest::class.java).orElse(null)

        return if (micronautTest == null) {
            null
        } else {
            val createBean = constructor != null && constructor.parameters.isNotEmpty()
            val context = MicronautKotlinTestContext(testClass, micronautTest, createBean)
            contexts[testClass.name] = context
            if (createBean) {
                context.bean
            } else {
                null
            }
        }
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
