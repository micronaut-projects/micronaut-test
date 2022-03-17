
package io.micronaut.test.kotest5

import io.kotest.assertions.fail
import io.kotest.core.spec.style.BehaviorSpec
import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
@Requires(property = "does.not.exist")
class RequiresTest: BehaviorSpec({

    given("a test with requires") {
        fail("Should never be executed")
    }

})
