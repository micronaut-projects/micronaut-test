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