
package io.micronaut.test.kotlintest

import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotlintest.MicronautKotlinTestExtension.getMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.math.pow
import kotlin.math.roundToInt

@MicronautTest
class MathCollaboratorTest(
        private val mathService: MathService,
        @Client("/") private val client: RxHttpClient // <2>
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
            val result = client.toBlocking().retrieve("/math/compute/$a", Int::class.java) // <3>
            result shouldBe b
            verify { mock.compute(a) } // <4>
        }

    }

}) {

    @MockBean(MathServiceImpl::class) // <1>
    fun mathService(): MathService {
        return mockk()
    }
}
