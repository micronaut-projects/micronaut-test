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
package io.micronaut.test.spock

import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import spock.lang.Specification

@MicronautTest
class PropertySourceMapSpec extends Specification implements TestPropertyProvider {
    @Property(name = "foo.bar")
    String val

    void "test inject properties"() {
        expect:
        val == 'one'
    }

    @Override
    Map<String, String> getProperties() {
        return ['foo.bar':'one', 'foo.baz':'two']
    }
}
