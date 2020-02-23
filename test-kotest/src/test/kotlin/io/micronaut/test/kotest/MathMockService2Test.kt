package io.micronaut.test.kotest

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.mockk.mockk

@MicronautTest
class MathMockService2Test(
        private val mathServices: Array<MathService>
): BehaviorSpec({

    given("an array of services") {
        `when`("the services are injected") {
            then("the array contains a single service") {
                mathServices.size shouldBe 1
            }
        }
    }

}) {

    @MockBean(MathServiceImpl::class)
    fun mathService(): MathService {
        return mockk()
    }
}
