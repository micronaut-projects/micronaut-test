package io.micronaut.test.kotlintest

import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.micronaut.context.annotation.Property
import io.micronaut.context.env.Environment
import io.micronaut.test.annotation.MicronautTest
import javax.inject.Inject

@MicronautTest
@Property(name = "foo.bar", value = "stuff")
class PropertyValueTest(@Inject val environment: Environment): AnnotationSpec() {

    @Test
    fun testInitialValue() {
        environment.getProperty("foo.bar", String::class.java).get() shouldBe "stuff"
    }

    @Property(name = "foo.bar", value = "changed")
    @Test
    fun testValueChanged() {
        environment.getProperty("foo.bar", String::class.java).get() shouldBe "changed"
    }

    @Test
    fun testValueRestored() {
        environment.getProperty("foo.bar", String::class.java).get() shouldBe "stuff"
    }
}