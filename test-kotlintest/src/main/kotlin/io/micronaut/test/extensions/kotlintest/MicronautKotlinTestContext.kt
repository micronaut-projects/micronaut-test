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

    fun afterSpec(spec: Spec) {
        commit()
        rollback()
    }

    fun afterSpecClass(spec: Spec) {
        afterClass(spec)
    }

    fun beforeSpec(spec: Spec) {
        begin()
    }

    fun beforeTest(testCase: TestCase) {
        beforeEach(testCase.spec, testCase.spec, testCase.test.javaClass)
    }

    fun getSpecDefinition() = specDefinition
}