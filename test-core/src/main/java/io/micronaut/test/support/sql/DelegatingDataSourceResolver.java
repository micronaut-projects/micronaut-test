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
package io.micronaut.test.support.sql;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.connection.jdbc.advice.DelegatingDataSource;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * Resolver for a {@link DelegatingDataSource}.
 * This will unwrap the datasource and resolve the underlying datasource.
 *
 * @since 4.1.0
 * @author Tim Yates
 */
@Singleton
@Experimental
@Requires(classes = {DataSource.class, DelegatingDataSource.class})
public class DelegatingDataSourceResolver implements DataSourceResolver {

    private static final Logger LOG = LoggerFactory.getLogger(DelegatingDataSourceResolver.class);

    @Override
    public int getOrder() {
        return DefaultDataSourceResolver.ORDER - 1;
    }

    @Override
    @NonNull
    public Optional<DataSource> resolve(@NonNull DataSource dataSource) {
        if (dataSource instanceof DelegatingDataSource delegatingDataSource) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Unwrapping and resolving data source: {}", delegatingDataSource);
            }
            return Optional.of(DelegatingDataSource.unwrapDataSource(delegatingDataSource));
        }
        return Optional.empty();
    }
}
