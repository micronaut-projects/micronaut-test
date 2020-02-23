package io.micronaut.test.kotest

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean

@MicronautTest
class MathInnerService2Test(
        private val mathService: MathService,
        private val services: Array<MathService>
): BehaviorSpec({

    given("an inner class mock") {

        `when`("the mock is called") {
            val result = mathService.compute(10)

            then("the mock is used") {
                result shouldBe 50
                mathService is MyService
                services.size shouldBe 1
            }

        }
    }

}) {

    @MockBean(MathServiceImpl::class)
    open class MyService : MathService {

        override fun compute(num: Int): Int {
            return num * 5
        }
    }
}

