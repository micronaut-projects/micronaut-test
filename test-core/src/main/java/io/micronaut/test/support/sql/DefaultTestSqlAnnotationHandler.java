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
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.test.annotation.Sql;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Default implementation of {@link TestSqlAnnotationHandler}.
 *
 * @since 4.1.0
 * @author Tim Yates
 */
@Singleton
@Experimental
@Requires(env = Environment.TEST)
public final class DefaultTestSqlAnnotationHandler implements TestSqlAnnotationHandler<DataSource> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultTestSqlAnnotationHandler.class);

    /**
     * Given a resource loader, a {@link Sql} annotation and a datasource, execute the SQL scripts in order.
     *
     * @param loader     The resource loader
     * @param sql        The Sql annotation
     * @param dataSource The datasource
     * @throws IOException  If the script cannot be read
     * @throws SQLException If the script cannot be executed
     */
    public void handleScript(
        ResourceLoader loader,
        Sql sql,
        DataSource dataSource
    ) throws IOException, SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing SQL scripts for datasource: {}", dataSource);
        }
        TestSqlHandlerUtils.handleScript(loader, sql, dataSource);
    }
}
