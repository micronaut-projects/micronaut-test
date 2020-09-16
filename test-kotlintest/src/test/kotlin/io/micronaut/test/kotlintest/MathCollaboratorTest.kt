/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.test.kotlintest

import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
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
