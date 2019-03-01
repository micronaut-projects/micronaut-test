package io.micronaut.test.kotlintest;

import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.verify
import javax.inject.Inject



@MicronautTest
class ApplicationRunAnotherTest(
        @Client("/") val client: RxHttpClient,
        val testService: TestService): BehaviorSpec({

    val specName = javaClass.simpleName

    given("testPingServer") {
        `when`("the mock is setup") {
            every { testService.doStuff() } returns "mocked by $specName"

            val response = client.toBlocking().retrieve(
                    HttpRequest.GET<String>("/test"), String::class.java)

            then("the response comes from the mock") {
                response shouldBe "mocked by ApplicationRunAnotherTest"
                verify {
                    testService.doStuff()
                }
            }
        }
    }

/*    @Test
    void testPingServer() {
        when(testService.doStuff()).thenReturn("mocked by " + ApplicationRunAnotherTest.class.getName());
        Assertions.assertEquals(
                "mocked by " +ApplicationRunAnotherTest.class.getName(),
                client.toBlocking().retrieve(HttpRequest.GET("/test"), String.class)
                );
        verify(testService).doStuff();
    }

    @Test
    void testPingServerAgain() {
        when(testService.doStuff()).thenReturn("changed by " + ApplicationRunAnotherTest.class.getName());
        Assertions.assertEquals(
                "changed by " +ApplicationRunAnotherTest.class.getName(),
                client.toBlocking().retrieve(HttpRequest.GET("/test"), String.class)
                );
        verify(testService).doStuff();
    }*/


}) {

    @MockBean(DefaultTestService::class)
    fun testService(): TestService {
        return mockk()
    }
}
