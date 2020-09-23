package io.micronaut.test.kotest

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.kotest.annotation.MicronautTest
import javax.inject.Inject

@MicronautTest(startApplication = false, rebuildContext = true)
internal class DisableEmbeddedApplicationTest: StringSpec() {

    @Inject lateinit var embeddedApplication: EmbeddedApplication<*>

    init {
        "test embedded server is not started"() {
            embeddedApplication.isRunning shouldBe false
        }

        "test embedded server is not started after context rebuild"() {
            embeddedApplication.isRunning shouldBe false
        }
    }
}
