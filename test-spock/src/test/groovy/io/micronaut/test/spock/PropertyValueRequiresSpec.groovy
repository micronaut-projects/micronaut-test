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
package io.micronaut.test.spock

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Issue
import spock.lang.Specification

import javax.inject.Inject
import javax.inject.Singleton

@Issue("https://github.com/micronaut-projects/micronaut-test/issues/91")
@MicronautTest(rebuildContext = true)
@Property(name = "foo.bar", value = "stuff")
class PropertyValueRequiresSpec extends Specification {
    @Inject
    MyService myService

    void "test initial value"() {
        expect:
        myService instanceof MyServiceStuff
    }

    @Property(name = "foo.bar", value = "changed")
    void "test value changed"() {
        expect:
        myService instanceof MyServiceChanged
    }

    void "test value restored"() {
        expect:
        myService instanceof MyServiceStuff
    }
}

interface MyService {}

@Singleton
@Requires(property = "foo.bar", value = "stuff")
class MyServiceStuff implements MyService {}


@Singleton
@Requires(property = "foo.bar", value = "changed")
class MyServiceChanged implements MyService {}