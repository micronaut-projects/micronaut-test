
package io.micronaut.test.kotlintest

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import javax.inject.Inject

@MicronautTest
class EmptyConstructorTest: StringSpec() {

    @Inject lateinit var mathService: MathService

    init {
        "test should be called"() {
            mathService.compute(1) shouldBe 4
        }
    }
}
