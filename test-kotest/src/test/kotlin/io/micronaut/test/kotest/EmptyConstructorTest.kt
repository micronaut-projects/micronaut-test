
package io.micronaut.test.kotest

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.kotest.annotation.MicronautTest
import jakarta.inject.Inject

@MicronautTest
class EmptyConstructorTest: StringSpec() {

    @Inject lateinit var mathService: MathService

    init {
        "test should be called" {
            mathService.compute(1) shouldBe 4
        }
    }
}
