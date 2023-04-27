/*
 * Copyright 2017-2021 original authors
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
package io.micronaut.test.support;

import java.util.Map;

/**
 * A test property provider factory is responsible for generating
 * a test property provider which is going to be called during test
 * setup, as any other {@link TestPropertyProvider}. However, such
 * a provider is discovered via service loading, and has access to
 * the current test class, as well as the currently available set
 * of resolved test properties.
 *
 * This can be used as an extension mechanism for modules which
 * can provide test properties, for example Micronaut Test Resources.
 *
 * @since 4.0.0
 */
@FunctionalInterface
public interface TestPropertyProviderFactory {
    /**
     * Creates a new test property provider.
     * @param availableProperties the test properties which are already
     * resolved at the point this method is called.
     * @param testClass the test class
     * @return a test property provider
     */
    TestPropertyProvider create(Map<String, Object> availableProperties, Class<?> testClass);
}
