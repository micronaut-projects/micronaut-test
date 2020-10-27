
package io.micronaut.test.kotlintest

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class SimpleTest: StringSpec() {

    init {
        "test should be called"() {
            1 shouldBe 1
        }
    }
}
