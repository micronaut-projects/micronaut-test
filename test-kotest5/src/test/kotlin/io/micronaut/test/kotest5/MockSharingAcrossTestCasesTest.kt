package io.micronaut.test.kotest5

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import io.micronaut.context.annotation.Requires
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension.getMock
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.mockk.MockKException
import io.mockk.every
import io.mockk.mockk
import jakarta.inject.Singleton

interface SharedMockService {
    fun compute(num: Int): Int
}

@Singleton
@Requires(property = "spec.name", value = "MockSharingAcrossTestCasesTest")
class DefaultSharedMockService : SharedMockService {
    override fun compute(num: Int) = num
}

/**
 * Mocks are refreshed once per root test case: everything nested in a root shares its mock and
 * stubs, including sibling branches, and the next root starts with a new mock.
 */
@MicronautTest
@io.micronaut.context.annotation.Property(name = "spec.name", value = "MockSharingAcrossTestCasesTest")
class MockSharingAcrossTestCasesTest(
    private val service: SharedMockService
) : BehaviorSpec({
    lateinit var firstRootMock: SharedMockService

    given("a first root") {
        `when`("the first branch stubs the mock") {
            firstRootMock = getMock(service)
            every { firstRootMock.compute(any()) } returns 9

            then("the stubbing applies in that branch") {
                service.compute(1) shouldBe 9
            }
        }

        `when`("a sibling branch runs") {
            then("it shares the mock and its stubs") {
                getMock(service) shouldBeSameInstanceAs firstRootMock
                service.compute(1) shouldBe 9
            }
        }
    }

    given("a second root") {
        `when`("nothing stubs the mock") {
            then("it has a new mock without the stubs of the first root") {
                getMock(service) shouldNotBeSameInstanceAs firstRootMock
                shouldThrow<MockKException> { service.compute(1) }
            }
        }
    }
}) {
    @MockBean(DefaultSharedMockService::class)
    fun service(): SharedMockService = mockk()
}
