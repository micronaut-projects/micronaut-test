package io.micronaut.test.kotlintest

import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.BehaviorSpec
import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MicronautTest

@MicronautTest(propertySources = ["myprops.properties"])
@Property(name = "supplied.value", value = "hello")
class PropertySourceTest(@Property(name = "foo.bar") val value: String,
                         @Property(name = "supplied.value") val suppliedValue: String) : BehaviorSpec({

    given("a property source") {
        `when`("the value is injected") {
            then("the correct value is injected") {
                value shouldBe "foo"
                suppliedValue shouldBe "hello"
            }
        }
    }

})

