/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.test.kotlintest

import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Value
import io.micronaut.test.annotation.MicronautTest

@MicronautTest
@Property(name = "foo.bar", value = "stuff")
class PropertyValueTest: AnnotationSpec() {

    @Value("\${foo.bar}")
    lateinit var value: String

    @Test
    fun testInitialValue() {
        value shouldBe "stuff"
    }

    @Property(name = "foo.bar", value = "changed")
    @Test
    fun testValueChanged() {
        value shouldBe "changed"
    }

    @Test
    fun testValueRestored() {
        value shouldBe "stuff"
    }
}