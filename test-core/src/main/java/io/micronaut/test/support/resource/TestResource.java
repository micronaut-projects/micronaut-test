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

import java.util.Map;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.value.PropertyResolver;

/**
 * Represents a test resource.
 *
 * @author graemerocher
 * @since 3.1.0
 */
public interface TestResource extends AutoCloseable {

    /**
     * Whether this resource is enabled. Defaults to {@code true}.
     *
     * <p>Note the passed environment represents the environment for the entire
     * test run and doesn't include test specific configuration.</p>
     *
     * @param environment The environment.
     * @return True if the resource is enabled
     */
    default boolean isEnabled(@NonNull PropertyResolver environment) {
        return true;
    }

    /**
     * Called before the first test is run.
     *
     * @param environment The environment the test runs in.
     * @return Additional properties added by this resource.
     */
    @NonNull
    Map<String, Object> start(@NonNull PropertyResolver environment) throws Exception;

    /**
     * Called after all tests have run.
     * @throws Exception If an error occurs closing this resource.
     */
    @Override
    void close() throws Exception;
}
