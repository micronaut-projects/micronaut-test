/*
 * Copyright 2017-2023 original authors
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
package io.micronaut.test.support.sql.resolver;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.order.Ordered;
import io.micronaut.test.support.sql.processor.SqlScriptProcessor;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * Resolves a {@link DataSource} to a concrete implementation that can be used to execute SQL scripts against.
 *
 * @since 4.1.0
 * @author Tim Yates
 */
@FunctionalInterface
public interface DataSourceResolver extends Ordered {

    /**
     * If the given datasource can be handled by this resolver, return an optional of the resolved datasource, otherwise empty.
     *
     * @param source The datasource to resolve
     * @return An optional of the resolved source if it's handled by this resolver
     */
    @NonNull
    Optional<? extends SqlScriptProcessor> resolve(@NonNull Object source);
}
