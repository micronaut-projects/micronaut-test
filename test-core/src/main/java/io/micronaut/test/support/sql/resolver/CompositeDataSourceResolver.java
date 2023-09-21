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

import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.test.support.sql.processor.SqlScriptProcessor;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;

/**
 * A {@link DataSourceResolver} that delegates to a list of other resolvers.
 * Available resolvers will be checked in turn, and the first that responds with a datasource will be used.
 *
 * @since 4.1.0
 * @author Tim Yates
 */
@Primary
@Singleton
@Internal
@Experimental
public class CompositeDataSourceResolver implements DataSourceResolver {

    private final List<DataSourceResolver> handlers;

    public CompositeDataSourceResolver(List<DataSourceResolver> handlers) {
        this.handlers = handlers;
    }

    @Override
    @NonNull
    public Optional<? extends SqlScriptProcessor> resolve(@NonNull Object source) {
        return handlers.stream()
            .map(handler -> handler.resolve(source))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }
}
