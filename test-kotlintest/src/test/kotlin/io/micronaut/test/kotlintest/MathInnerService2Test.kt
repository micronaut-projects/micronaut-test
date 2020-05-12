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
import io.micronaut.test.annotation.MockBean

@MicronautTest
class MathInnerService2Test(
        private val mathService: MathService,
        private val services: Array<MathService>
): BehaviorSpec({

    given("an inner class mock") {

        `when`("the mock is called") {
            val result = mathService.compute(10)

            then("the mock is used") {
                result shouldBe 50
                mathService is MyService
                services.size shouldBe 1
            }

        }
    }

}) {

    @MockBean(MathServiceImpl::class)
    open class MyService : MathService {

        override fun compute(num: Int): Int {
            return num * 5
        }
    }
}

