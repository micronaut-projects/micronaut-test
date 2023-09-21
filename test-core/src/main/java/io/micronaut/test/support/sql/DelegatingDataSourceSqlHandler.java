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

import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.Internal;
import io.micronaut.data.connection.jdbc.advice.DelegatingDataSource;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * Handler for {@link DataSource} instances which may be a {@link DelegatingDataSource}.
 *
 * @since 4.1.0
 * @author Tim Yates
 */
@Singleton
@Internal
@Experimental
@Requires(classes = {DelegatingDataSource.class, DataSource.class})
@Replaces(DataSourceSqlHandler.class)
public class DelegatingDataSourceSqlHandler extends DataSourceSqlHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DelegatingDataSourceSqlHandler.class);

    @Override
    public void handle(DataSource dataSource, String sql) {
        if (dataSource instanceof DelegatingDataSource delegatingDataSource) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Unwrapping DelegatingDataSource: {}", delegatingDataSource);
            }
            dataSource = delegatingDataSource.getTargetDataSource();
        }
        super.handle(dataSource, sql);
    }
}
