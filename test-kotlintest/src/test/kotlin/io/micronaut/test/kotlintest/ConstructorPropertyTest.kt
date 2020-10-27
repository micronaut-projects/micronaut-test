
package io.micronaut.test.kotlintest

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Value
import io.micronaut.test.extensions.junit5.annotation.MicronautTest

@MicronautTest
@Property(name = "foo.bar", value = "3")
class ConstructorPropertyTest(
        @Value("\${foo.bar}") private val value: Int,
        @Property(name = "foo.bar") private val property: Int
): StringSpec({

  "test the values are injected"() {
      value shouldBe 3
      property shouldBe 3
  }

})
