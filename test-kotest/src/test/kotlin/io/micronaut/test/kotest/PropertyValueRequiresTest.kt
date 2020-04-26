package io.micronaut.test.kotest

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.beInstanceOf
import io.kotest.matchers.should
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.annotation.MicronautTest
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(rebuildContext = true)
@Property(name = "foo.bar", value = "stuff")
class PropertyValueRequiresTest: AnnotationSpec() {

    @Inject
    lateinit var myService: MyService

    @Test
    fun testInitialValue() {
        myService should beInstanceOf<MyServiceStuff>()
    }

    @Property(name = "foo.bar", value = "changed")
    @Test
    fun testValueChanged() {
        myService should beInstanceOf<MyServiceChanged>()
    }

    @Test
    fun testValueRestored() {
        myService should beInstanceOf<MyServiceStuff>()
    }
}

interface MyService

@Singleton
@Requires(property = "foo.bar", value = "stuff")
open class MyServiceStuff : MyService


@Singleton
@Requires(property = "foo.bar", value = "changed")
open class MyServiceChanged : MyService
