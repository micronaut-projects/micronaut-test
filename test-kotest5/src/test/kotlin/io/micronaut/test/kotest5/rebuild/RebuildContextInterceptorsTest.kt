package io.micronaut.test.kotest5.rebuild

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.context.TestContext
import io.micronaut.test.context.TestExecutionListener
import io.micronaut.test.context.TestMethodInterceptor
import io.micronaut.test.context.TestMethodInvocationContext
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * Each test must run inside the interceptors of the context it uses, and get its listeners from it,
 * although Kotest enters the test case extension before the test listeners.
 */
@MicronautTest(rebuildContext = true)
@Property(name = "spec.name", value = "RebuildContextInterceptorsTest")
class RebuildContextInterceptorsTest : AnnotationSpec() {

    @Inject
    lateinit var applicationContext: ApplicationContext

    @Inject
    lateinit var interceptor: RebuildRecordingInterceptor

    @Inject
    lateinit var listener: RebuildRecordingListener

    private val seenContexts = mutableListOf<ApplicationContext>()

    @Test
    fun firstTestRunsInsideTheInterceptorsOfItsOwnContext() {
        assertRanInsideItsOwnContext()
    }

    @Test
    fun secondTestRunsInsideTheInterceptorsOfItsOwnContext() {
        assertRanInsideItsOwnContext()
    }

    @Test
    fun thirdTestRunsInsideTheInterceptorsOfItsOwnContext() {
        assertRanInsideItsOwnContext()
    }

    private fun assertRanInsideItsOwnContext() {
        applicationContext.isRunning.shouldBeTrue()
        interceptor.interceptedContext shouldBeSameInstanceAs applicationContext
        interceptor.testContextApplicationContext shouldBeSameInstanceAs applicationContext
        listener.beforeTestMethodContext shouldBeSameInstanceAs applicationContext
        seenContexts shouldNotContain applicationContext
        seenContexts += applicationContext
        interceptor.interceptions shouldBe 1
    }
}

@Singleton
@Requires(property = "spec.name", value = "RebuildContextInterceptorsTest")
open class RebuildRecordingInterceptor(
    private val applicationContext: ApplicationContext,
) : TestMethodInterceptor<Any> {
    @Volatile
    var interceptedContext: ApplicationContext? = null

    @Volatile
    var testContextApplicationContext: ApplicationContext? = null

    @Volatile
    var interceptions = 0

    override fun interceptTest(methodInvocationContext: TestMethodInvocationContext<Any>): Any {
        interceptedContext = applicationContext
        testContextApplicationContext = methodInvocationContext.testContext.applicationContext
        interceptions++
        return methodInvocationContext.proceed()
    }
}

@Singleton
@Requires(property = "spec.name", value = "RebuildContextInterceptorsTest")
open class RebuildRecordingListener : TestExecutionListener {
    @Volatile
    var beforeTestMethodContext: ApplicationContext? = null

    override fun beforeTestMethod(testContext: TestContext) {
        beforeTestMethodContext = testContext.applicationContext
    }
}
