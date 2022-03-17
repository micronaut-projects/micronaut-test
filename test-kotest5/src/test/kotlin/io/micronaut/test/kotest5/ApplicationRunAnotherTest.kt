
package io.micronaut.test.kotest5

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension.getMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

@MicronautTest
class ApplicationRunAnotherTest(
        @Client("/") private val client: HttpClient,
        private val testService: io.micronaut.test.kotest5.TestService
): BehaviorSpec({

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

    @MockBean(io.micronaut.test.kotest5.DefaultTestService::class)
    fun testService(): io.micronaut.test.kotest5.TestService {
        return mockk()
    }
}
