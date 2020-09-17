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
package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.annotation.MicronautTest;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.inject.Inject;
import javax.inject.Singleton;

// https://github.com/micronaut-projects/micronaut-test/issues/91
@MicronautTest(rebuildContext = true)
@Property(name = "foo.bar", value = "stuff")
@TestMethodOrder(OrderAnnotation.class)
class PropertyValueRequiresTest {

    @Inject
    MyService myService;

    @Test
    @Order(1)
    void testInitialValue() {
        MatcherAssert.assertThat(myService, IsInstanceOf.instanceOf(MyServiceStuff.class));
    }

    @Property(name = "foo.bar", value = "changed")
    @Test
    @Order(2)
    void testValueChanged() {
        MatcherAssert.assertThat(myService, IsInstanceOf.instanceOf(MyServiceChanged.class));
    }

    @Test
    @Order(3)
    void testValueRestored() {
        MatcherAssert.assertThat(myService, IsInstanceOf.instanceOf(MyServiceStuff.class));
    }

}

interface MyService {}

@Singleton
@Requires(property = "foo.bar", value = "stuff")
class MyServiceStuff implements MyService {}


@Singleton
@Requires(property = "foo.bar", value = "changed")
class MyServiceChanged implements MyService {}
