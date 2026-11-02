package io.micronaut.test.kotest5.meta

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import io.micronaut.test.extensions.AbstractMicronautExtension
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import jakarta.inject.Inject

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@MicronautTest(transactional = false, environments = ["meta"])
annotation class NonTransactionalMicronautTest

@NonTransactionalMicronautTest
internal class MetaAnnotationTest : StringSpec() {

    @Inject
    lateinit var applicationContext: ApplicationContext

    init {
        "attributes of the meta-annotation apply" {
            applicationContext.environment.activeNames.contains("meta") shouldBe true
            applicationContext.getProperty(AbstractMicronautExtension.TEST_TRANSACTIONAL, Boolean::class.java).orElse(null) shouldBe false
        }
    }
}
