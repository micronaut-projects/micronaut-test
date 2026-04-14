package io.micronaut.test.junit5

import io.micronaut.aop.InterceptedProxy
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@MicronautTest
open class KotlinJunitTest {

    @Inject
    lateinit var application: EmbeddedApplication<*>

    @Inject
    lateinit var echoService: EchoService

    @Test
    fun testItWorks() {
        Assertions.assertTrue(application.isRunning)
    }

    @Test
    fun echoServiceUsesMockBeanInstance() {
        Assertions.assertFalse(echoService is InterceptedProxy<*>)
        Assertions.assertTrue(echoService is MockEchoService)
        Assertions.assertEquals("mocked: hello", echoService.echo("hello"))
    }

    @MockBean(EchoServiceImpl::class)
    open fun echoService(): EchoService {
        return MockEchoService()
    }
}

interface EchoService {
    fun echo(message: String): String
}

class MockEchoService : EchoService {
    override fun echo(message: String): String {
        return "mocked: $message"
    }
}

@Singleton
open class EchoServiceImpl : EchoService {
    override fun echo(message: String): String {
        return message
    }
}
