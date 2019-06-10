package io.micronaut.test.extensions.kotlintest

import io.kotlintest.Spec
import io.kotlintest.TestCase
import io.kotlintest.TestCaseConfig
import io.micronaut.aop.InterceptedProxy
import io.micronaut.inject.BeanDefinition
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.AbstractMicronautExtension
import java.lang.reflect.Field
import kotlin.reflect.KClass

class MicronautKotlinTestContext(testClass: Class<Any>, micronautTest: MicronautTest) : AbstractMicronautExtension<Spec>() {

    val bean : Spec

    init {
        beforeClass(null, testClass, micronautTest)
        bean = applicationContext.getBean(testClass) as Spec
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