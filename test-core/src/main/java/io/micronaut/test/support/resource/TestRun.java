/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.test.support.resource;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.value.PropertyResolver;

/**
 * Strategy object used by {@link TestResourceManager} to configure the test run.
 *
 * @since 3.1.0
 * @author graemerocher
 */
public interface TestRun {
    /**
     * @return The environment.
     */
    @NonNull PropertyResolver getEnvironment();

    /**
     * Add a property that will be included in all applications for the test run.
     *
     * @param name The property name
     * @param value The value
     * @return This test run
     */
    @NonNull
    TestRun addProperty(@NonNull String name, @Nullable Object value);
}
