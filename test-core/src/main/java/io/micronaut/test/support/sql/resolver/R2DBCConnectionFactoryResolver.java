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
import io.micronaut.data.connection.jdbc.advice.DelegatingDataSource;
import io.micronaut.test.support.sql.processor.SqlScriptProcessor;
import io.micronaut.test.support.sql.processor.R2DBCConnectionFactoryProcessor;
import io.r2dbc.spi.ConnectionFactory;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Resolver for a {@link DelegatingDataSource}.
 * This will unwrap the datasource and resolve the underlying datasource.
 *
 * @since 4.1.0
 * @author Tim Yates
 */
@Singleton
@Internal
@Experimental
@Requires(classes = {ConnectionFactory.class})
public class R2DBCConnectionFactoryResolver implements DataSourceResolver {

    private static final Logger LOG = LoggerFactory.getLogger(R2DBCConnectionFactoryResolver.class);

    @Override
    public int getOrder() {
        return DefaultDataSourceResolver.ORDER - 1;
    }

    @Override
    @NonNull
    public Optional<? extends SqlScriptProcessor> resolve(@NonNull Object source) {

        if (source instanceof ConnectionFactory connectionFactory) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Found a connection factory: {}", connectionFactory);
            }
            return Optional.of(new R2DBCConnectionFactoryProcessor(connectionFactory));
        }
        return Optional.empty();
    }
}
