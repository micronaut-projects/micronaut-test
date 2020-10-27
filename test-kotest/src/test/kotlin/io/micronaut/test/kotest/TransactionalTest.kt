
package io.micronaut.test.kotest

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import io.micronaut.test.extensions.kotest.annotation.MicronautTest
import io.micronaut.test.transaction.spring.SpringTransactionTestExecutionListener

@MicronautTest(transactional = true)
@DbProperties
class TransactionalTest(
        private val applicationContext: ApplicationContext) : BehaviorSpec({

    given("a test") {
        `when`("the test is transactional") {
            then("the SpringTransactionTestExecutionListener does exist") {
                applicationContext.containsBean(SpringTransactionTestExecutionListener::class.java) shouldBe true
            }
        }
    }

})

