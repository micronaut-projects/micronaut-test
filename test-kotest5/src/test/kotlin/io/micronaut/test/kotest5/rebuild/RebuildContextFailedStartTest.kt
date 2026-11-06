package io.micronaut.test.kotest5.rebuild

import io.kotest.core.annotation.EnabledCondition
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.Spec
import io.kotest.core.spec.SpecRef
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.TestEngineLauncher
import io.kotest.engine.listener.CollectingTestEngineListener
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.micronaut.test.kotest5.ProjectConfig
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

private const val ENABLED = "rebuild.failed-start-fixture"
private const val FAIL_STARTUP = "rebuild.fail-startup"

class FailedStartFixtureEnabled : EnabledCondition {
    override fun evaluate(kclass: KClass<out Spec>) = System.getProperty(ENABLED) != null
}

/**
 * The method properties of the first test stop its rebuilt context from starting. Kotest must
 * report that as a failure of that test, not of the whole spec.
 */
@EnabledIf(FailedStartFixtureEnabled::class)
@MicronautTest(rebuildContext = true)
class RebuildContextFailedStartFixture : AnnotationSpec() {

    @Inject
    lateinit var applicationContext: ApplicationContext

    @Test
    @Property(name = FAIL_STARTUP, value = "true")
    fun contextFailsToStart() {
    }

    @Test
    fun nextTestStillRuns() {
        applicationContext.isRunning shouldBe true
    }
}

/**
 * The context starts, but disables the bean of the spec, so there is nothing to run a test against.
 */
@EnabledIf(FailedStartFixtureEnabled::class)
@Requires(property = "rebuild.no-bean-fixture-bean")
@MicronautTest(rebuildContext = true)
class RebuildContextNoBeanFixture : AnnotationSpec() {

    @Test
    fun firstTest() {
        throw IllegalStateException("must not run without a bean")
    }

    @Test
    fun secondTest() {
        throw IllegalStateException("must not run without a bean")
    }
}

@Context
@Requires(property = FAIL_STARTUP, value = "true")
class RebuildFailedStartBean {
    init {
        throw IllegalStateException("startup failure")
    }
}

class RebuildContextFailedStartTest : StringSpec({

    "a context that fails to start fails the test that needed it" {
        System.setProperty(ENABLED, "true")
        try {
            val collector = CollectingTestEngineListener()
            CompletableFuture.supplyAsync {
                runBlocking {
                    TestEngineLauncher()
                        .withListener(collector)
                        .withProjectConfig(ProjectConfig)
                        .withSpecRefs(SpecRef.Reference(RebuildContextFailedStartFixture::class))
                        .execute()
                }
            }.get(60, TimeUnit.SECONDS)

            val results = collector.tests.mapKeys { it.key.name.name }
            results.keys shouldBe setOf("contextFailsToStart", "nextTestStillRuns")
            results.getValue("contextFailsToStart").isErrorOrFailure shouldBe true
            results.getValue("nextTestStillRuns").isSuccess shouldBe true
        } finally {
            System.clearProperty(ENABLED)
        }
    }

    "a spec without a bean is still ignored when it rebuilds its context" {
        System.setProperty(ENABLED, "true")
        try {
            val collector = CollectingTestEngineListener()
            CompletableFuture.supplyAsync {
                runBlocking {
                    TestEngineLauncher()
                        .withListener(collector)
                        .withProjectConfig(ProjectConfig)
                        .withSpecRefs(SpecRef.Reference(RebuildContextNoBeanFixture::class))
                        .execute()
                }
            }.get(60, TimeUnit.SECONDS)

            val results = collector.tests.mapKeys { it.key.name.name }
            results.keys shouldBe setOf("firstTest", "secondTest")
            results.values.all { it.isIgnored } shouldBe true
        } finally {
            System.clearProperty(ENABLED)
        }
    }
})
