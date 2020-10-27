
package io.micronaut.test.kotlintest

import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import io.micronaut.test.extensions.junit5.annotation.MicronautTest

@MicronautTest // <1>
class MathServiceTest(
        private val mathService: MathService // <2>
): BehaviorSpec({

    given("the math service") {

        `when`("the service is called with 2") {
            val result = mathService.compute(2) // <3>
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
