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
package io.micronaut.test.kotlintest

import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.specs.AnnotationSpec
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.annotation.MicronautTest
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(rebuildContext = true)
@Property(name = "foo.bar", value = "stuff")
class PropertyValueRequiresTest: AnnotationSpec() {

    @Inject
    lateinit var myService: MyService

    @Test
    fun testInitialValue() {
        myService.shouldBeInstanceOf<MyServiceStuff>()
    }

    @Property(name = "foo.bar", value = "changed")
    @Test
    fun testValueChanged() {
        myService.shouldBeInstanceOf<MyServiceChanged>()
    }

    @Test
    fun testValueRestored() {
        myService.shouldBeInstanceOf<MyServiceStuff>()
    }
}

interface MyService

@Singleton
@Requires(property = "foo.bar", value = "stuff")
open class MyServiceStuff : MyService


@Singleton
@Requires(property = "foo.bar", value = "changed")
open class MyServiceChanged : MyService