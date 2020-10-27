
package io.micronaut.test.kotlintest

import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Value
import io.micronaut.test.extensions.junit5.annotation.MicronautTest

@MicronautTest
@Property(name = "foo.bar", value = "stuff")
class PropertyValueTest: AnnotationSpec() {

    @Value("\${foo.bar}")
    lateinit var value: String

    @Test
    fun testInitialValue() {
        value shouldBe "stuff"
    }

    @Property(name = "foo.bar", value = "changed")
    @Test
    fun testValueChanged() {
        value shouldBe "changed"
    }

    @Test
    fun testValueRestored() {
        value shouldBe "stuff"
    }
}