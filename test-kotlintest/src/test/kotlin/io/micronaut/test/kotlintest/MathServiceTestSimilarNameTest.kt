
package io.micronaut.test.kotlintest

import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotlintest.MicronautKotlinTestExtension.getMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

@MicronautTest
class MathServiceTestSimilarNameTest(private val mathService: MathService): BehaviorSpec({

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
