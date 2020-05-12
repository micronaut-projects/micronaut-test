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
package io.micronaut.test.junit5;

import io.micronaut.context.DefaultApplicationContextBuilder;
import io.micronaut.core.annotation.Introspected;

import java.util.Collections;

@Introspected
public class CustomContextBuilder extends DefaultApplicationContextBuilder {
    public CustomContextBuilder() {
        properties(Collections.singletonMap(
                "custom.builder.prop", "value"
        ));
    }
}
