
package io.micronaut.test.kotest5

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Value
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
class TestPropertiesFactoryCtorInjectionTest(
    @Value("\${this-test-class}") val thisTestClass: String
) : StringSpec() {
    init {
        "property should be injected" {
            thisTestClass shouldBe TestPropertiesFactoryCtorInjectionTest::class.qualifiedName
        }
    }
}
