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

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.test.support.sql.processor.SqlDataSourceProcessor;
import io.micronaut.test.support.sql.processor.SqlScriptProcessor;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * Resolver for a vanilla {@link DataSource}.
 *
 * @since 4.1.0
 * @author Tim Yates
 */
@Singleton
@Internal
@Experimental
@Requires(classes = {DataSource.class})
public class DefaultDataSourceResolver implements DataSourceResolver {

    static final int ORDER = 1;

    private static final Logger LOG = LoggerFactory.getLogger(DefaultDataSourceResolver.class);

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    @NonNull
    public Optional<? extends SqlScriptProcessor> resolve(@NonNull Object source) {
        if (source instanceof DataSource dataSource) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Resolving data source: {}", source);
            }
            return Optional.of(new SqlDataSourceProcessor(dataSource));
        } else {
            return Optional.empty();
        }
    }
}
