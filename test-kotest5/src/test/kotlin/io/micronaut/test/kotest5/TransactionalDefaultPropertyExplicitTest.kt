package io.micronaut.test.kotest5

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest(transactional = true)
@Property(name = "micronaut.test.transactional-default", value = "false")
class TransactionalDefaultPropertyExplicitTest(
    private val applicationContext: ApplicationContext
) : StringSpec() {

    init {
        "explicit transactional annotation wins over default property" {
            applicationContext.environment
                .getProperty("micronaut.test.transactional", Boolean::class.java)
                .orElse(false) shouldBe true
        }
    }
}
