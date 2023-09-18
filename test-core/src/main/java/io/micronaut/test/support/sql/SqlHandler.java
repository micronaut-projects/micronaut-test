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

import io.micronaut.core.io.ResourceLoader;
import io.micronaut.test.annotation.Sql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Utility class for handling SQL scripts.
 *
 * @since 4.1.0
 * @author Tim Yates
 */
public final class SqlHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SqlHandler.class);

    private SqlHandler() {
    }

    /**
     * Given a resource loader, a {@link Sql} annotation and a datasource, execute the SQL scripts in order.
     *
     * @param loader The resource loader
     * @param sql The Sql annotation
     * @param dataSource The datasource
     * @throws IOException If the script cannot be read
     * @throws SQLException If the script cannot be executed
     */
    public static void handleScript(
        ResourceLoader loader,
        Sql sql,
        DataSource dataSource
    ) throws IOException, SQLException {
        for (String script : sql.scripts()) {
            Optional<URL> resource = loader.getResource(script);
            if (resource.isPresent()) {
                try (
                    InputStream in = resource.get().openStream();
                    Connection connection = dataSource.getConnection();
                    Statement statement = connection.createStatement()
                ) {
                    String scriptBody = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    statement.execute(scriptBody);
                }
            } else {
                LOG.warn("Could not find SQL script: {}", script);
            }
        }
    }
}
