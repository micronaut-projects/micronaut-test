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
import io.micronaut.core.annotation.Internal;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Handler for raw {@link DataSource} instances.
 *
 * @since 4.1.0
 * @author Tim Yates
 */
@Singleton
@Internal
@Experimental
@Requires(classes = {DataSource.class})
public class DataSourceSqlHandler implements SqlHandler<DataSource> {

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceSqlHandler.class);

    @Override
    public void handle(DataSource dataSource, String sql) throws SQLException {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()
        ) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{}: Executing SQL: {}", dataSource, sql);
            }
            statement.execute(sql);
        }
    }
}
