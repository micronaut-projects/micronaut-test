package io.micronaut.test.kotest5

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
@Property(name = "micronaut.test.transactional-default", value = "false")
class TransactionalDefaultPropertyTest(
    private val applicationContext: ApplicationContext
) : StringSpec() {

    init {
        "transactions disabled when default property false" {
            applicationContext.environment
                .getProperty("micronaut.test.transactional", Boolean::class.java)
                .orElse(true) shouldBe false
        }
    }
}
