
package io.micronaut.test.kotest5

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe


class SimpleTest: StringSpec() {

    init {
        "test should be called"() {
            1 shouldBe 1
        }
    }
}
