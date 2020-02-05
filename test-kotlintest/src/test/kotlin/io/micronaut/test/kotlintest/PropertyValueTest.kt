package io.micronaut.test.kotlintest

import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MicronautTest

@MicronautTest
@Property(name = "foo.bar", value = "stuff")
class PropertyValueTest(@Property(name = "foo.bar") val value: String): AnnotationSpec() {

    @Test
    fun testInitialValue() {
        value shouldBe "stuff"
    }

    @Property(name = "foo.bar", value = "changed")
    @Test
    @Ignore
    fun testValueChanged() {
        value shouldBe "changed"
    }

    @Test
    fun testValueRestored() {
        value shouldBe "stuff"
    }
}