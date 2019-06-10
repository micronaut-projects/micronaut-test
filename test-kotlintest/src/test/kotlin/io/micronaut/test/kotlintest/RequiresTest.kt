package io.micronaut.test.kotlintest

import io.kotlintest.fail
import io.kotlintest.specs.BehaviorSpec
import io.micronaut.context.annotation.Requires
import io.micronaut.test.annotation.MicronautTest

@MicronautTest
@Requires(property = "does.not.exist")
class RequiresTest: BehaviorSpec({

    given("a test with requires") {
        fail("Should never be executed")
    }

})
