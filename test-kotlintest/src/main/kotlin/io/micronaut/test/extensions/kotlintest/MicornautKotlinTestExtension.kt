package io.micronaut.test.extensions.kotlintest

import io.kotlintest.Spec
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.extensions.ConstructorExtension
import io.kotlintest.extensions.TestListener
import io.micronaut.test.annotation.MicronautTest
import org.junit.platform.commons.support.AnnotationSupport
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

object MicornautKotlinTestExtension: TestListener, ConstructorExtension {

    val contexts: MutableMap<Spec, MicronautKotlinTestContext> = mutableMapOf()

    override fun afterSpec(spec: Spec) {
        contexts[spec]?.afterSpec(spec)
    }

    override fun afterSpecClass(spec: Spec, results: Map<TestCase, TestResult>) {
        contexts[spec]?.afterSpecClass(spec)
    }

    override fun beforeSpec(spec: Spec) {
        contexts[spec]?.beforeSpec(spec)
    }

    override fun beforeTest(testCase: TestCase) {
        contexts[testCase.spec]?.beforeTest(testCase)
    }

    override fun <T : Spec> instantiate(clazz: KClass<T>): Spec? {
        // we only instantiate via spring if there's actually parameters in the constructor
        // otherwise there's nothing to inject there
        val constructor = clazz.primaryConstructor
        val testClass: Class<Any> = clazz.java as Class<Any>
        val micronautTest = AnnotationSupport.findAnnotation<MicronautTest>(testClass, MicronautTest::class.java).orElse(null)
        return if (constructor == null || constructor.parameters.isEmpty() && micronautTest != null) {
            null
        } else {
            val context = MicronautKotlinTestContext(testClass, micronautTest)
            val bean: Spec = context.bean
            contexts[bean] = context
            bean
        }
    }

}