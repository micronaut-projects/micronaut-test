
package io.micronaut.test.kotlintest

import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
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