package io.micronaut.test.kotest5

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import io.micronaut.test.annotation.MicronautTestValue
import io.micronaut.test.annotation.TransactionMode
import io.micronaut.test.extensions.kotest5.MicronautKotest5Context
import java.lang.ref.WeakReference

/**
 * A spec's context object can outlive the spec, so it must let go of its application context once
 * the spec has finished.
 */
class ContextReleasedTest : StringSpec({

    "the application context is unreachable once the spec has finished" {
        val spec = this.testCase.spec
        @Suppress("UNCHECKED_CAST")
        val micronautContext = MicronautKotest5Context(
            ContextReleasedTest::class.java as Class<Any>,
            MicronautTestValue(
                Void.TYPE, emptyArray(), emptyArray(), emptyArray(), false, false, false,
                emptyArray(), TransactionMode.SEPARATE_TRANSACTIONS, false, false, false
            ),
            false
        )
        micronautContext.beforeSpecClass(spec)
        val context = WeakReference<ApplicationContext>(micronautContext.buildContext(spec).applicationContext)
        micronautContext.afterSpecClass(spec)

        var attempts = 0
        while (attempts++ < 100 && context.get() != null) {
            System.gc()
            Thread.sleep(100)
        }

        context.get() shouldBe null
    }
})
