package io.micronaut.test.extensions.kotlintest

import io.kotlintest.Spec
import io.kotlintest.TestCase
import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.extensions.AbstractMicronautExtension
import io.micronaut.test.support.TestPropertyProvider
import kotlin.reflect.full.memberFunctions

class MicronautKotlinTestContext(private val testClass: Class<Any>,
                                 private val micronautTest: MicronautTest,
                                 private val createBean: Boolean) : AbstractMicronautExtension<Spec>() {

    override fun resolveTestProperties(context: Spec?, testAnnotation: MicronautTest?, testProperties: MutableMap<String, Any>?) {
        if (context is TestPropertyProvider) {
            testProperties?.putAll(context.properties)
        }
    }

    val bean : Spec?

    init {
        bean = if (createBean) {
            beforeClass(null, testClass, micronautTest)
            applicationContext.findBean(testClass).orElse(null) as Spec?
        } else {
            null
        }
    }

    override fun alignMocks(context: Spec?, instance: Any) {
    }

    fun beforeSpecClass(spec: Spec) {
        if (!createBean) {
            beforeClass(spec, testClass, micronautTest)
            applicationContext.inject(spec)
        }
    }

    fun afterSpecClass(spec: Spec) {
        afterClass(spec)
    }

    fun beforeTest(testCase: TestCase) {
        var filter = testCase.spec::class.memberFunctions.filter { it.name == testCase.name }
        var propertyAnnotations: List<Property>? = emptyList()
        if (filter.isNotEmpty()) {
            propertyAnnotations = filter.first().annotations.filter { it is Property } as? List<Property>
        }
        beforeEach(testCase.spec, testCase.spec, testCase.test.javaClass, propertyAnnotations)
        begin()
    }

    fun afterTest(testCase: TestCase) {
        commit()
        rollback()
    }

    fun getSpecDefinition() = specDefinition
}
