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
package io.micronaut.test.support.sql.processor;

import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Processes SQL scripts against a {@link DataSource}.
 *
 * @since 4.1.0
 * @author Tim Yates
 */
@Experimental
public class SqlDataSourceProcessor implements SqlScriptProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(SqlDataSourceProcessor.class);

    private final DataSource dataSource;

    public SqlDataSourceProcessor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void process(@NonNull String sql) throws SQLException {
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
