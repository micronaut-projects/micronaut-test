package io.micronaut.test.kotest5

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.micronaut.transaction.test.DefaultTestTransactionExecutionListener

@MicronautTest(transactional = true)
@DbProperties
class TransactionalTest(
        private val applicationContext: ApplicationContext) : BehaviorSpec({

    given("a test") {
        `when`("the test is transactional") {
            then("the SpringTransactionTestExecutionListener does exist") {
                applicationContext.containsBean(DefaultTestTransactionExecutionListener::class.java) shouldBe true
            }
        }
    }

})

