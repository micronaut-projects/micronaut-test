/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.test.kotlintest

import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import io.micronaut.test.annotation.MicronautTest

@MicronautTest // <1>
class MathServiceTest(
        private val mathService: MathService // <2>
): BehaviorSpec({

    given("the math service") {

        `when`("the service is called with 2") {
            val result = mathService.compute(2) // <3>
            then("the result is 8") {
                result shouldBe 8
            }
        }

        `when`("the service is called with 3") {
            val result = mathService.compute(3)
            then("the result is 12") {
                result shouldBe 12
            }
        }
    }
})
