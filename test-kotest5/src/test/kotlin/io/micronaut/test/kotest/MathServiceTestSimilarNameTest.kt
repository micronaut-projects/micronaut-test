
package io.micronaut.test.kotest

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.kotest.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest.MicronautKotest5Extension.getMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

@MicronautTest
class MathServiceTestSimilarNameTest(private val mathService: MathService) : BehaviorSpec({

    given("test similarly named test suites dont leak mocks") {

        `when`("the mock is provided") {
            val mock = getMock(mathService)
            every { mock.compute(10) } returns 20

            then("the mock is used") {
                mock.compute(10) shouldBe 20
                verify { mock.compute(10) }
            }
        }
    }

}) {

    @MockBean(MathServiceImpl::class)
    fun mathService(): MathService {
        return mockk()
    }
}
