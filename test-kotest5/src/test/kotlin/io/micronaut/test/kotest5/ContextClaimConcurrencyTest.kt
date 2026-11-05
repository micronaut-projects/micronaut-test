package io.micronaut.test.kotest5

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.concurrency.TestExecutionMode
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import io.micronaut.test.extensions.kotest5.MicronautKotest5Context
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import jakarta.inject.Inject
import java.util.concurrent.BrokenBarrierException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread

private const val INSTANCES = 3

/** Only registers contexts here, since its instances have no tests to start them for. */
@MicronautTest
class ClaimedContextFixture : StringSpec()

/**
 * Instances of a spec without constructor parameters can run their callbacks on separate threads,
 * and each has to end up with a context of its own.
 */
class ContextClaimConcurrencyTest : StringSpec({

    "concurrent instances claim different contexts" {
        val specs = List(INSTANCES) {
            MicronautKotest5Extension.instantiate(ClaimedContextFixture::class)
            ClaimedContextFixture()
        }
        // Lines the threads up between lookup and claim. With atomic claiming only one gets there at
        // a time, so the wait runs out and each thread carries on alone.
        val barrier = CyclicBarrier(INSTANCES)
        MicronautKotest5Extension.beforeContextClaim = {
            try {
                barrier.await(500, TimeUnit.MILLISECONDS)
            } catch (_: TimeoutException) {
            } catch (_: BrokenBarrierException) {
            }
        }
        try {
            val claims = specs.map { spec ->
                val claim = CompletableFuture<MicronautKotest5Context?>()
                thread(isDaemon = true) { claim.complete(MicronautKotest5Extension.contextOf(spec)) }
                claim
            }

            claims.map { it.get(30, TimeUnit.SECONDS) }.distinct() shouldHaveSize INSTANCES
        } finally {
            MicronautKotest5Extension.beforeContextClaim = {}
            MicronautKotest5Extension.contexts.remove(ClaimedContextFixture::class.java.name)
        }
    }
})

/**
 * Concurrent roots of a spec without constructor parameters each get a running context injected.
 */
@MicronautTest
class ConcurrentFieldInjectedRootsTest : StringSpec() {

    @Inject
    lateinit var context: ApplicationContext

    override fun isolationMode() = IsolationMode.InstancePerRoot

    init {
        testExecutionMode = TestExecutionMode.Concurrent

        "first root" { context.isRunning shouldBe true }
        "second root" { context.isRunning shouldBe true }
        "third root" { context.isRunning shouldBe true }
    }
}
