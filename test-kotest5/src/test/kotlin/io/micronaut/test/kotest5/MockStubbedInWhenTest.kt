package io.micronaut.test.kotest5

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Requires
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension.getMock
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import jakarta.inject.Singleton

interface WhenStubbedService {
    fun compute(num: Int): Int
}

@Singleton
@Requires(property = "spec.name", value = "MockStubbedInWhenTest")
class WhenStubbedCaller(private val service: WhenStubbedService) {
    fun runCompute(num: Int) = service.compute(num)
}

@Singleton
@Requires(property = "spec.name", value = "MockStubbedInWhenTest")
class DefaultWhenStubbedService : WhenStubbedService {
    override fun compute(num: Int) = num * 4
}

/**
 * A mock stubbed in a `when` container is used by the `then` test nested in it.
 */
@MicronautTest
@io.micronaut.context.annotation.Property(name = "spec.name", value = "MockStubbedInWhenTest")
class MockStubbedInWhenTest(
    private val service: WhenStubbedService,
    private val caller: WhenStubbedCaller
) : BehaviorSpec({
    given("a mock stubbed in when") {
        `when`("the mock is provided") {
            val mock = getMock(service)
            every { mock.compute(any()) } returns 9

            then("the stubbing applies in then") {
                caller.runCompute(3) shouldBe 9
            }
        }
    }
}) {
    @MockBean(DefaultWhenStubbedService::class)
    fun service(): WhenStubbedService = mockk()
}
