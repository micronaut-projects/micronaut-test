package io.micronaut.test.kotlintest

import io.kotest.matchers.shouldBe
import io.kotest.core.spec.style.StringSpec

class SimpleTest: StringSpec() {

    init {
        "test should be called"() {
            1 shouldBe 1
        }
    }
}
