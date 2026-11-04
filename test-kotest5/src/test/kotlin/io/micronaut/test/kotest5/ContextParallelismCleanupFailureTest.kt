package io.micronaut.test.kotest5

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.annotation.MicronautTestValue
import io.micronaut.test.annotation.TransactionMode
import io.micronaut.test.context.TestContext
import io.micronaut.test.context.TestExecutionListener
import io.micronaut.test.extensions.AbstractMicronautExtension
import io.micronaut.test.extensions.kotest5.MicronautKotest5Context
import jakarta.inject.Singleton
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

private const val SPEC_NAME = "ContextParallelismCleanupFailure"
private const val FAILURE = "afterTestClass failed"

@Property(name = "spec.name", value = SPEC_NAME)
private class FailingCleanupTarget

private class SucceedingCleanupTarget

@Singleton
@Requires(property = "spec.name", value = SPEC_NAME)
class FailingCleanupListener : TestExecutionListener {
    override fun afterTestClass(testContext: TestContext) {
        throw IllegalStateException(FAILURE)
    }
}

class ContextParallelismCleanupFailureTest : StringSpec({

    fun newContext(target: Class<*>, spec: Spec): MicronautKotest5Context {
        @Suppress("UNCHECKED_CAST")
        val context = MicronautKotest5Context(
            target as Class<Any>,
            MicronautTestValue(
                Void.TYPE, emptyArray(), emptyArray(), emptyArray(), false, false, false,
                emptyArray(), TransactionMode.SEPARATE_TRANSACTIONS, false, false, false
            ),
            false
        )
        context.beforeSpecClass(spec)
        return context
    }

    "a failing afterTestClass listener still releases the context parallelism permit" {
        val spec = this.testCase.spec
        System.setProperty(AbstractMicronautExtension.CONTEXT_PARALLELISM_PROPERTY, "1")
        try {
            val failing = newContext(FailingCleanupTarget::class.java, spec)

            shouldThrow<IllegalStateException> { failing.afterSpecClass(spec) }.message shouldBe FAILURE
            failing.isApplicationContextOpen() shouldBe false

            // A permit that is never released blocks the second context forever, so bound the wait
            val next = CompletableFuture.supplyAsync { newContext(SucceedingCleanupTarget::class.java, spec) }
                .get(60, TimeUnit.SECONDS)
            next.afterSpecClass(spec)
        } finally {
            System.clearProperty(AbstractMicronautExtension.CONTEXT_PARALLELISM_PROPERTY)
        }
    }
})
