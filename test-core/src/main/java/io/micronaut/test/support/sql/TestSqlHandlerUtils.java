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

import io.micronaut.core.annotation.Experimental;
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
 * Utility class that performs the SQL script execution.
 *
 * @since 4.1.0
 * @author Tim Yates
 */
@Experimental
final class TestSqlHandlerUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TestSqlHandlerUtils.class);

    private TestSqlHandlerUtils() {
    }

    static void handleScript(
        ResourceLoader loader,
        Sql sql,
        DataSource dataSource
    ) throws IOException, SQLException {
        for (String script : sql.value()) {
            Optional<URL> resource = loader.getResource(script);
            if (resource.isPresent()) {
                try (
                    InputStream in = resource.get().openStream();
                    Connection connection = dataSource.getConnection();
                    Statement statement = connection.createStatement()
                ) {
                    String scriptBody = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("For connection {} executing SQL script: {}", connection, scriptBody);
                    }
                    statement.execute(scriptBody);
                }
            } else {
                LOG.warn("Could not find SQL script: {}", script);
            }
        }
    }
}
