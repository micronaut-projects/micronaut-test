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
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
@Property(name = "foo.bar", value = "stuff")
@TestMethodOrder(OrderAnnotation.class)
class PropertyValueTest {

    @Property(name = "foo.bar")
    String val;

    @Test
    @Order(1)
    void testInitialValue() {
        assertEquals("stuff", val);
    }

    @Property(name = "foo.bar", value = "changed")
    @Test
    @Order(2)
    void testValueChanged() {
        assertEquals("changed", val);
    }

    @Test
    @Order(3)
    void testValueRestored() {
        assertEquals("stuff", val);
    }
}
