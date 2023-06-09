package io.micronaut.test.kotest5.intercept

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.micronaut.test.kotest5.MathService

@MicronautTest
class InterceptTest(
    private val mathService: MathService,
    private val interceptor: TestInterceptor
) : BehaviorSpec({

    given("the math service") {

        `when`("the service is called with 2") {
            val result = mathService.compute(2) // <3>
            then("the result is 8") {
                result shouldBe 8
            }
        }

        `when`("validate invocations") {
            interceptor.calls shouldBe listOf(
                "BEFORE TEST CLASS InterceptTest",
                "BEFORE TEST EXECUTION the math service",
                "BEFORE TEST METHOD the math service",
                "BEFORE TEST EXECUTION the math service",
                "BEFORE TEST EXECUTION the service is called with 2",
                "BEFORE TEST METHOD the service is called with 2",
                "BEFORE TEST EXECUTION the service is called with 2",
                "BEFORE TEST METHOD the result is 8",
                "BEFORE TEST EXECUTION the result is 8",
                "AFTER TEST EXECUTION the result is 8",
                "AFTER TEST METHOD the result is 8",
                "AFTER TEST EXECUTION the service is called with 2",
                "AFTER TEST METHOD the service is called with 2",
                "AFTER TEST EXECUTION the service is called with 2",
                "BEFORE TEST EXECUTION validate invocations",
                "BEFORE TEST METHOD validate invocations",
                "BEFORE TEST EXECUTION validate invocations"
            )
        }
    }
})
