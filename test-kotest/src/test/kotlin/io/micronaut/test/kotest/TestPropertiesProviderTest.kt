/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.test.kotest

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import javax.inject.Inject

@MicronautTest
class TestPropertiesProviderTest: StringSpec(), TestPropertyProvider {

    override fun getProperties(): MutableMap<String, String> {
        return mutableMapOf("foo.bar" to "3")
    }

    @set:Inject
    @setparam:Property(name = "foo.bar")
    var property: Int = 0

    init {
        "test the property was injected"() {
            property shouldBe 3
        }
    }
}
