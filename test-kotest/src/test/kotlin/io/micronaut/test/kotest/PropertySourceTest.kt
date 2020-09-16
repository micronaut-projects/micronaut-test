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
package io.micronaut.test.kotest

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MicronautTest

@MicronautTest(propertySources = ["myprops.properties"])
@Property(name = "supplied.value", value = "hello")
class PropertySourceTest(@Property(name = "foo.bar") val value: String,
                         @Property(name = "supplied.value") val suppliedValue: String) : BehaviorSpec({

    given("a property source") {
        `when`("the value is injected") {
            then("the correct value is injected") {
                value shouldBe "foo"
                suppliedValue shouldBe "hello"
            }
        }
    }

})

