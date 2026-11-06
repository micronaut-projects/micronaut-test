package io.micronaut.test.kotest5.rebuild

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.context.scope.Refreshable
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import jakarta.annotation.PreDestroy
import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * A `@Refreshable` bean created while the rebuilt context starts must be refreshed by the
 * refresh the extension sends before each test, which only happens when the extension sends it to
 * the new context's refresh scope rather than to the stopped context's.
 */
@MicronautTest(rebuildContext = true)
@Property(name = "spec.name", value = "RebuildContextRefreshScopeTest")
class RebuildContextRefreshScopeTest : AnnotationSpec() {

    @Inject
    lateinit var applicationContext: ApplicationContext

    @Inject
    lateinit var startupRecorder: RebuildStartupRecorder

    @Inject
    lateinit var refreshableBean: RebuildRefreshableBean

    @Test
    fun firstTestRefreshesTheRebuiltContext() {
        assertRefreshedInRebuiltContext()
    }

    @Test
    fun secondTestRefreshesTheRebuiltContext() {
        assertRefreshedInRebuiltContext()
    }

    private fun assertRefreshedInRebuiltContext() {
        applicationContext.isRunning.shouldBeTrue()
        val atStartup = startupRecorder.atStartup.shouldNotBeNull()
        atStartup.disposed.shouldBeTrue()
        val current = refreshableBean.self()
        current shouldNotBeSameInstanceAs atStartup
        current.disposed.shouldBeFalse()
    }
}

@Refreshable
@Requires(property = "spec.name", value = "RebuildContextRefreshScopeTest")
open class RebuildRefreshableBean {
    @Volatile
    open var disposed = false

    open fun self(): RebuildRefreshableBean = this

    @PreDestroy
    open fun dispose() {
        disposed = true
    }
}

@Singleton
@Requires(property = "spec.name", value = "RebuildContextRefreshScopeTest")
open class RebuildStartupRecorder(
    private val refreshableBean: RebuildRefreshableBean,
) : ApplicationEventListener<StartupEvent> {
    @Volatile
    var atStartup: RebuildRefreshableBean? = null

    override fun onApplicationEvent(event: StartupEvent) {
        atStartup = refreshableBean.self()
    }
}
