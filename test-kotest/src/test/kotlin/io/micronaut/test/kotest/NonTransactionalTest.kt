
package io.micronaut.test.kotest

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import io.micronaut.test.extensions.kotest.annotation.MicronautTest
import io.micronaut.transaction.test.DefaultTestTransactionExecutionListener

@MicronautTest(transactional = false)
@DbProperties
class NonTransactionalTest(
        private val applicationContext: ApplicationContext) : BehaviorSpec({

    given("a test") {
        `when`("the test is not transactional") {
            then("the SpringTransactionTestExecutionListener does not exist") {
                applicationContext.containsBean(DefaultTestTransactionExecutionListener::class.java) shouldBe false
            }
        }
    }

})

