package io.micronaut.test.extensions.kotlintest

import io.kotlintest.Spec
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.extensions.ConstructorExtension
import io.kotlintest.extensions.TestCaseExtension
import io.kotlintest.extensions.TestListener
import io.kotlintest.extensions.TopLevelTest
import io.micronaut.aop.InterceptedProxy
import io.micronaut.test.annotation.MicronautTest
import org.junit.platform.commons.support.AnnotationSupport
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor

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
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        contexts[testCase.spec.javaClass.name]?.afterTest(testCase)
    }

    override fun <T : Spec> instantiate(clazz: KClass<T>): Spec? {
        // we only instantiate via spring if there's actually parameters in the constructor
        // otherwise there's nothing to inject there
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
