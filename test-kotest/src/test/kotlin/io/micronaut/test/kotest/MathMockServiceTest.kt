package io.micronaut.test.kotest

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest.MicronautKotestExtension.getMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

import kotlin.math.pow
import kotlin.math.roundToInt

@MicronautTest
class MathMockServiceTest(
        private val mathService: MathService // <3>
): BehaviorSpec({

    given("test compute num to square") {

        `when`("the mock is provided") {
            val mock = getMock(mathService) // <4>
            every { mock.compute(any()) } answers {
                firstArg<Int>().toDouble().pow(2).roundToInt()
            }

            then("the mock implementation is used") {
                mock.compute(3) shouldBe 9
                verify { mock.compute(3) } // <5>
            }
        }
    }

}) {

    @MockBean(MathServiceImpl::class) // <1>
    fun mathService(): MathService {
        return mockk() // <2>
    }
}
