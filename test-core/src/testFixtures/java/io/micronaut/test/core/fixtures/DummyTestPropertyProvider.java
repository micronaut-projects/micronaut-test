/*
 * Copyright 2003-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.test.core.fixtures;

import io.micronaut.test.support.TestPropertyProvider;

import java.util.Map;

public class DummyTestPropertyProvider implements TestPropertyProvider {
    private final Map<String, Object> availableProperties;
    private final Class<?> testClass;

    public DummyTestPropertyProvider(Map<String, Object> availableProperties, Class<?> testClass) {
        this.availableProperties = availableProperties;
        this.testClass = testClass;
    }

    @Override
    public Map<String, String> getProperties() {
        var fromConf = availableProperties.getOrDefault("some.property", "not found");
        return Map.of(
            "this-test-class", testClass.getName(),
            "derived-from-config", fromConf + " and derived in provider"
        );
    }
}
