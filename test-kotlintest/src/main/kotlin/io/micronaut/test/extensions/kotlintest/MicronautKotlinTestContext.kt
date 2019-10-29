package io.micronaut.test.extensions.kotlintest

import io.kotlintest.Spec
import io.kotlintest.TestCase
import io.kotlintest.TestCaseConfig
import io.micronaut.aop.InterceptedProxy
import io.micronaut.inject.BeanDefinition
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.AbstractMicronautExtension
import io.micronaut.test.support.TestPropertyProvider
import java.lang.reflect.Field
import kotlin.reflect.KClass

class MicronautKotlinTestContext(testClass: Class<Any>, micronautTest: MicronautTest) : AbstractMicronautExtension<Spec>() {
    override fun resolveTestProperties(context: Spec?, testAnnotation: MicronautTest?, testProperties: MutableMap<String, Any>?) {
        if (context is TestPropertyProvider) {
            testProperties?.putAll(context.properties)
        }
    }

    val bean : Spec?

    init {
        beforeClass(null, testClass, micronautTest)
        bean = applicationContext.findBean(testClass).orElse(null) as Spec?
    }

    override fun alignMocks(context: Spec?, instance: Any) {
    }

    fun afterSpecClass(spec: Spec) {
        afterClass(spec)
    }

    fun beforeTest(testCase: TestCase) {
        beforeEach(testCase.spec, testCase.spec, testCase.test.javaClass)
        begin()
    }

    fun afterTest(testCase: TestCase) {
        commit()
        rollback()
    }

    fun getSpecDefinition() = specDefinition
}
