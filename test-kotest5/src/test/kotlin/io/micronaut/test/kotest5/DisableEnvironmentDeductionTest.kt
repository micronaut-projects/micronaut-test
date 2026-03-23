package io.micronaut.test.kotest5

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.env.Environment
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import jakarta.inject.Inject

@MicronautTest(deduceEnvironment = false)
internal class DisableEnvironmentDeductionTest : StringSpec() {

    @Inject
    lateinit var environment: Environment

    init {
        "environment deduction property is false" {
            environment.getProperty(Environment.DEDUCE_ENVIRONMENT_PROPERTY, Boolean::class.java).orElse(null) shouldBe false
        }
    }
}
