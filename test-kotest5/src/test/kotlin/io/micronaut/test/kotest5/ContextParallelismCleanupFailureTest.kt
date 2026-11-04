package io.micronaut.test.kotest5

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.SpecRef
import io.kotest.engine.TestEngineLauncher
import io.micronaut.context.ApplicationContext
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlin.reflect.KClass
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
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit

private const val SPEC_NAME = "ContextParallelismCleanupFailure"
private const val SETUP_SPEC_NAME = "ContextParallelismSetupFailure"
private const val CONSTRUCTOR_SPEC_NAME = "ContextParallelismConstructorFailure"
private const val FAILURE = "afterTestClass failed"

@Property(name = "spec.name", value = SPEC_NAME)
private class FailingCleanupTarget

private class SucceedingCleanupTarget

@Property(name = "spec.name", value = SETUP_SPEC_NAME)
private class FailingSetupTarget

@Singleton
@Requires(property = "spec.name", value = SETUP_SPEC_NAME)
class FailingSetupListener : TestExecutionListener {
    override fun beforeTestClass(testContext: TestContext) {
        throw IllegalStateException(FAILURE)
    }
}

class MissingConstructorDependency

@Singleton
@Requires(property = "spec.name", value = CONSTRUCTOR_SPEC_NAME)
@Property(name = "spec.name", value = CONSTRUCTOR_SPEC_NAME)
class FailingConstructorTarget(private val missing: MissingConstructorDependency)

private fun testValue() = MicronautTestValue(
    Void.TYPE, emptyArray(), emptyArray(), emptyArray(), false, false, false,
    emptyArray(), TransactionMode.SEPARATE_TRANSACTIONS, false, false, false
)

/** Starts nothing at limit 1 unless a permit is free, bounded so a leaked permit fails the test. */
private fun assertPermitIsFree(spec: Spec) {
    @Suppress("UNCHECKED_CAST")
    val next = CompletableFuture.supplyAsync {
        val context = MicronautKotest5Context(SucceedingCleanupTarget::class.java as Class<Any>, testValue(), false)
        context.beforeSpecClass(spec)
        context
    }.get(60, TimeUnit.SECONDS)
    next.afterSpecClass(spec)
}

private fun runSpec(spec: KClass<out Spec>) {
    CompletableFuture.supplyAsync {
        runBlocking {
            TestEngineLauncher()
                .withNoOpListener()
                .withProjectConfig(ProjectConfig)
                .withSpecRefs(SpecRef.Reference(spec))
                .execute()
        }
    }.get(60, TimeUnit.SECONDS)
}

/** Runs normally too, where nothing limits it; the tests below also run it at limit 1. */
@MicronautTest
class MultipleInstancesFixture(private val context: ApplicationContext) : StringSpec() {
    override fun isolationMode() = IsolationMode.InstancePerRoot

    init {
        "first root" { context.isRunning shouldBe true }
        "second root" { context.isRunning shouldBe true }
    }
}

/** Kotest creates this spec but never runs its callbacks. */
@MicronautTest
class NoEnabledTestsFixture(private val context: ApplicationContext) : StringSpec() {
    init {
        "disabled".config(enabled = false) { context.isRunning shouldBe true }
    }
}

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

    fun atLimitOne(block: () -> Unit) {
        System.setProperty(AbstractMicronautExtension.CONTEXT_PARALLELISM_PROPERTY, "1")
        try {
            // on its own thread, so that a permit leaked by an earlier test fails this one rather than hanging it
            val task = FutureTask { block() }
            thread(isDaemon = true) { task.run() }
            task.get(90, TimeUnit.SECONDS)
        } finally {
            System.clearProperty(AbstractMicronautExtension.CONTEXT_PARALLELISM_PROPERTY)
        }
    }

    "a failing beforeTestClass listener still releases the context parallelism permit" {
        val spec = this.testCase.spec
        atLimitOne {
            shouldThrow<IllegalStateException> { newContext(FailingSetupTarget::class.java, spec) }.message shouldBe FAILURE
            assertPermitIsFree(spec)
        }
    }

    "a failing constructor injection still releases the context parallelism permit" {
        val spec = this.testCase.spec
        atLimitOne {
            shouldThrowAny {
                @Suppress("UNCHECKED_CAST")
                MicronautKotest5Context(FailingConstructorTarget::class.java as Class<Any>, testValue(), true)
            }
            assertPermitIsFree(spec)
        }
    }

    "the instances of a spec share the context parallelism permit" {
        val spec = this.testCase.spec
        atLimitOne {
            runSpec(MultipleInstancesFixture::class)
            assertPermitIsFree(spec)
        }
    }

    "a spec without enabled tests releases the context parallelism permit" {
        val spec = this.testCase.spec
        atLimitOne {
            runSpec(NoEnabledTestsFixture::class)
            assertPermitIsFree(spec)
        }
    }
})
