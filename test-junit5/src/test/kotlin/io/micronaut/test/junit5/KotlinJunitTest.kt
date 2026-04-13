package io.micronaut.test.junit5

import io.micronaut.aop.InterceptedProxy
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

@MicronautTest
open class KotlinJunitTest {

    @Inject
    lateinit var application: EmbeddedApplication<*>

    @Inject
    lateinit var echoService: EchoServiceImpl

    @Test
    fun testItWorks() {
        Assertions.assertTrue(application.isRunning)
    }

    @Test
    fun echoServiceIsMocked() {
        Assertions.assertFalse(echoService is InterceptedProxy<*>)
        Assertions.assertTrue(Mockito.mockingDetails(echoService).isMock)
    }

    @MockBean(EchoServiceImpl::class)
    open fun echoService(): EchoServiceImpl {
        return Mockito.mock(EchoServiceImpl::class.java)
    }
}

@Singleton
open class EchoServiceImpl {
    open fun echo(message: String): String {
        return message
    }
}
