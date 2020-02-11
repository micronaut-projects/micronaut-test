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
package io.micronaut.test.support;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Allows providing application properties dynamically from a test.
 *
 * @author graemerocher
 * @since 1.1.0
 */
@FunctionalInterface
public interface TestPropertyProvider extends Supplier<Map<String, String>> {

    @Override
    default Map<String, String> get() {
        return getProperties();
    }

    /**
     * Allows dynamically providing properties for a test.
     * @return A map of properties
     */
    @Nonnull
    Map<String, String> getProperties();
}
