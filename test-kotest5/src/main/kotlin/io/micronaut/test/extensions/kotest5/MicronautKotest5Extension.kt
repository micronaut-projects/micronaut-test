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
package io.micronaut.test.extensions.kotest5

import io.kotest.core.extensions.ConstructorExtension
import io.kotest.core.extensions.TestCaseExtension
import io.kotest.core.listeners.FinalizeSpecListener
import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import io.micronaut.aop.InterceptedProxy
import io.micronaut.test.annotation.AnnotationUtils
import io.micronaut.test.annotation.MicronautTestValue
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

object MicronautKotest5Extension : TestListener, ConstructorExtension, TestCaseExtension, FinalizeSpecListener {

    override suspend fun intercept(
        testCase: TestCase,
        execute: suspend (TestCase) -> TestResult,
    ): TestResult {
        val context = testCase.spec.context()
        return if (context != null && context.isSpecBeanMissing()) {
            // It's a MicronautTest test where the bean doesn't exist
            TestResult.Ignored
        } else {
            // Not a MicronautTest test or the bean exists
            context?.interceptTestCase(testCase, execute) ?: execute(testCase)
        }
    }

    val contexts: MutableMap<String, MutableList<MicronautKotest5Context>> = mutableMapOf()

    private fun Spec.context(): MicronautKotest5Context? = synchronized(contexts) {
        val specContexts = contexts[javaClass.name] ?: return null
        // Kotest creates an instance without constructor parameters itself, so the instance is only
        // known here. Each one takes the next unclaimed context, since each has its own. Instances
        // can run their callbacks on separate threads, so claiming one has to be atomic.
        specContexts.find { it.bean == this || it.claimedBy == this }
            ?: specContexts.find { it.bean == null && it.claimedBy == null }?.also {
                beforeContextClaim()
                it.claimedBy = this
            }
    }

    internal fun contextOf(spec: Spec) = spec.context()

    /**
     * Lets a test hold a thread between finding an unclaimed context and claiming it.
     */
    @Volatile
    internal var beforeContextClaim: () -> Unit = {}

    /**
     * Removes the context from the [contexts] map
     */
    private fun MicronautKotest5Context.cleanupContext() {
        val specClassName: String? = this.bean?.javaClass?.name
        synchronized(contexts) {
            contexts[specClassName]?.let {
                it.remove(this)
                if (it.isEmpty()) contexts.remove(specClassName)
            }
        }
    }

    override suspend fun beforeSpec(spec: Spec) {
        spec.context()?.beforeSpecClass(spec)
    }

    override suspend fun finalizeSpec(kclass: KClass<out Spec>, results: Map<TestCase, TestResult>) {
        // Kotest skips beforeSpec and afterSpec for a spec without enabled tests
        synchronized(contexts) { contexts.remove(kclass.java.name) }?.forEach { it.stopIfRunning() }
    }

    override suspend fun afterSpec(spec: Spec) {
        spec.context()?.let {
            try {
                it.afterSpecClass(spec)
            } finally {
                it.cleanupContext()
            }
        }
    }

    override suspend fun beforeTest(testCase: TestCase) {
        testCase.spec.context()?.beforeTest(testCase)
    }

    override suspend fun afterTest(testCase: TestCase, result: TestResult) {
        testCase.spec.context()?.afterTest(testCase, result)
    }

    override suspend fun beforeInvocation(testCase: TestCase, iteration: Int) {
        testCase.spec.context()?.beforeInvocation(testCase)
    }

    override suspend fun afterInvocation(testCase: TestCase, iteration: Int) {
        testCase.spec.context()?.afterInvocation(testCase)
    }

    override suspend fun beforeContainer(testCase: TestCase) {
        testCase.spec.context()?.beforeInvocation(testCase)
    }

    override suspend fun afterContainer(testCase: TestCase, result: TestResult) {
        testCase.spec.context()?.afterInvocation(testCase)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Spec> instantiate(clazz: KClass<T>): Spec? {
        val constructor = clazz.primaryConstructor
        val testClass: Class<Any> = clazz.java as Class<Any>
        val micronautTestValue = AnnotationUtils.findAnnotation(testClass, MicronautTest::class.java)
            .map { micronautTest -> buildValueObject(micronautTest) }
            .orElse(null)
        return if (micronautTestValue == null) {
            null
        } else {
            val createBean = constructor != null && constructor.parameters.isNotEmpty()
            // the isolation modes create further instances while the first is running, on its thread
            val sharesPermit = synchronized(contexts) { contexts[testClass.name]?.isNotEmpty() == true }
            val context = MicronautKotest5Context(testClass, micronautTestValue, createBean, sharesPermit)
            synchronized(contexts) { contexts.getOrPut(testClass.name, ::mutableListOf).add(context) }
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
            micronautTest.transactionMode,
            micronautTest.startApplication,
            false,
            micronautTest.deduceEnvironment,
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
