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

import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MicronautTestValue
import io.micronaut.test.context.TestContext
import io.micronaut.test.extensions.AbstractMicronautExtension
import io.micronaut.test.support.TestPropertyProvider
import kotlin.reflect.full.memberFunctions

class MicronautKotestContext(private val testClass: Class<Any>,
                             private val micronautTestValue: MicronautTestValue,
                             private val createBean: Boolean) : AbstractMicronautExtension<Spec>() {

    override fun resolveTestProperties(context: Spec?, testAnnotationValue: MicronautTestValue, testProperties: MutableMap<String, Any>?) {
        if (context is TestPropertyProvider) {
            testProperties?.putAll(context.properties)
        }
    }

    val bean : Spec?

    init {
        bean = if (createBean) {
            beforeClass(null, testClass, micronautTestValue)
            applicationContext.findBean(testClass).orElse(null) as Spec?
        } else {
            null
        }
    }

    override fun alignMocks(context: Spec?, instance: Any) {
    }

    fun beforeSpecClass(spec: Spec) {
        if (!createBean) {
            beforeClass(spec, testClass, micronautTestValue)
            applicationContext.inject(spec)
        }
        beforeTestClass(buildContext(spec))
    }

    fun afterSpecClass(spec: Spec) {
        afterTestClass(buildContext(spec))
        afterClass(spec)
    }

    @Suppress("UNCHECKED_CAST")
    fun beforeTest(testCase: TestCase) {
        var filter = testCase.spec::class.memberFunctions.filter { it.name == testCase.name }
        var propertyAnnotations: List<Property>? = emptyList()
        if (filter.isNotEmpty()) {
            propertyAnnotations = filter.first().annotations.filter { it is Property } as? List<Property>
        }
        beforeEach(testCase.spec, testCase.spec, testCase.test.javaClass, propertyAnnotations)
        beforeTestMethod(buildContext(testCase, null))
    }

    fun afterTest(testCase: TestCase, result: TestResult) {
        afterTestMethod(buildContext(testCase, result))
    }

    fun beforeInvocation(testCase: TestCase) {
        beforeTestExecution(buildContext(testCase, null))
    }

    fun afterInvocation(testCase: TestCase) {
        afterTestExecution(buildContext(testCase, null))
    }

    fun getSpecDefinition() = specDefinition

    fun buildContext(spec: Spec): TestContext {
        return TestContext(applicationContext, spec.javaClass, null, spec, null)
    }

    fun buildContext(testCase: TestCase, result: TestResult?): TestContext {
        return TestContext(applicationContext, testCase.spec.javaClass, testCase.test.javaClass, testCase.spec, result?.error)
    }

}
