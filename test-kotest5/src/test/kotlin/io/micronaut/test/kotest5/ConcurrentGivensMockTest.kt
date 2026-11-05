package io.micronaut.test.kotest5

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.engine.concurrency.TestExecutionMode
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Requires
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension.getMock
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import jakarta.inject.Singleton
import kotlinx.coroutines.delay

interface ConcurrentGivensService {
    fun compute(num: Int): Int
}

@Singleton
@Requires(property = "spec.name", value = "ConcurrentGivensMockTest")
class DefaultConcurrentGivensService : ConcurrentGivensService {
    override fun compute(num: Int) = num
}

/**
 * Concurrent givens keep their own mocks with InstancePerRoot, where each has its own context. In
 * one shared context, the next given to start would replace the mock of the one still running.
 */
@MicronautTest
@io.micronaut.context.annotation.Property(name = "spec.name", value = "ConcurrentGivensMockTest")
class ConcurrentGivensMockTest(private val service: ConcurrentGivensService) : BehaviorSpec() {

    override fun isolationMode() = IsolationMode.InstancePerRoot

    @MockBean(DefaultConcurrentGivensService::class)
    fun service(): ConcurrentGivensService = mockk()

    init {
        testExecutionMode = TestExecutionMode.Concurrent

        given("a first given") {
            `when`("it stubs its mock") {
                val mock = getMock(service)
                every { mock.compute(any()) } returns 1
                delay(300)

                then("the stubbing still applies") {
                    service.compute(0) shouldBe 1
                }
            }
        }

        given("a second given") {
            `when`("it stubs its mock") {
                val mock = getMock(service)
                every { mock.compute(any()) } returns 2
                delay(300)

                then("the stubbing still applies") {
                    service.compute(0) shouldBe 2
                }
            }
        }
    }
}
