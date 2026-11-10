package io.micronaut.test.kotest5

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension.getMock
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.inject.Inject
import jakarta.inject.Singleton

interface NestedSetupService {
    fun compute(num: Int): Int
}

@Singleton
@Requires(property = "spec.name", value = "NestedTestsGetNewMocksTest")
class DefaultNestedSetupService : NestedSetupService {
    override fun compute(num: Int) = num
}

/**
 * Outside a BehaviorSpec each nested test starts with a new mock.
 */
@MicronautTest
@io.micronaut.context.annotation.Property(name = "spec.name", value = "NestedTestsGetNewMocksTest")
class NestedTestsGetNewMocksTest(private val service: NestedSetupService) : DescribeSpec({
    fun computeOnceAndVerify() {
        val mock = getMock(service)
        every { mock.compute(any()) } returns 1
        service.compute(1)
        verify(exactly = 1) { mock.compute(any()) }
    }

    describe("a service") {
        it("first") { computeOnceAndVerify() }
        it("second") { computeOnceAndVerify() }
    }
}) {
    @MockBean(DefaultNestedSetupService::class)
    fun service(): NestedSetupService = mockk()
}

/**
 * With rebuildContext = true each nested test gets its own context.
 */
@MicronautTest(rebuildContext = true)
class NestedTestsRebuildContextTest : FunSpec() {

    @Inject
    lateinit var context: ApplicationContext

    init {
        val seen = mutableListOf<ApplicationContext>()
        context("nested") {
            test("first") {
                seen.add(context)
                seen.distinct().size shouldBe seen.size
            }
            test("second") {
                seen.add(context)
                seen.distinct().size shouldBe seen.size
            }
        }
    }
}
