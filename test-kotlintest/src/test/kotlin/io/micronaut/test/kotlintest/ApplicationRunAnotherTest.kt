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
package io.micronaut.test.kotlintest;

import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotlintest.MicronautKotlinTestExtension.getMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

@MicronautTest
class ApplicationRunAnotherTest(
        @Client("/") private val client: RxHttpClient,
        private val testService: TestService): BehaviorSpec({

    val specName = javaClass.simpleName

    given("testPingServer") {
        `when`("the mock is setup") {
            val mock = getMock(testService)

            every { mock.doStuff() } returns "mocked by $specName"

            val response = client.toBlocking().retrieve(
                    HttpRequest.GET<String>("/test"), String::class.java)

            then("the response comes from the mock") {
                response shouldBe "mocked by ApplicationRunAnotherTest"
                verify {
                    mock.doStuff()
                }
            }
        }

        `when`("the mock is changed") {
            val mock = getMock(testService)

            every { mock.doStuff() } returns "changed by $specName"

            val response = client.toBlocking().retrieve(
                    HttpRequest.GET<String>("/test"), String::class.java)

            then("the mock was changed") {
                response shouldBe "changed by ApplicationRunAnotherTest"
                verify {
                    mock.doStuff()
                }
            }
        }
    }

}) {

    @MockBean(DefaultTestService::class)
    fun testService(): TestService {
        return mockk()
    }
}
