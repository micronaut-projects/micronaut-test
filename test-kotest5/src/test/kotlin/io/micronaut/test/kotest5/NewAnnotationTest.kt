package io.micronaut.test.kotest5

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
class NewAnnotationTest(
        private val mathService: MathService
) : BehaviorSpec({

    given("the math service") {

        `when`("the service is called with 2") {
            val result = mathService.compute(2)
            then("the result is 8") {
                result shouldBe 8
            }
        }

        `when`("the service is called with 3") {
            val result = mathService.compute(3)
            then("the result is 12") {
                result shouldBe 12
            }
        }
    }
})