package io.micronaut.test.kotlintest

import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotlintest.MicornautKotlinTestExtension.getMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyAll
import kotlin.math.pow
import kotlin.math.roundToInt

@MicronautTest
class MathCollaboratorTest(
        private val mathService: MathService,
        @Client("/") private val client: RxHttpClient
): StringSpec({

    "test compute num to square" {
        val mock = getMock(mathService)

        every { mock.compute(any()) } answers {
            firstArg<Int>().toDouble().pow(2).roundToInt()
        }

        forall(
                row(2, 4),
                row(3, 9)
        ) { a: Int, b: Int ->
            val result = client.toBlocking().retrieve("/math/compute/$a", Int::class.java)
            result shouldBe b
            verify { mock.compute(a) }
        }

    }

}) {

    @MockBean(MathServiceImpl::class)
    fun mathService(): MathService {
        return mockk()
    }
}
