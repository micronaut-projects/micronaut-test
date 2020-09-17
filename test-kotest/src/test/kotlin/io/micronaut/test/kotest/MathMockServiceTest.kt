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
) : BehaviorSpec({

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
