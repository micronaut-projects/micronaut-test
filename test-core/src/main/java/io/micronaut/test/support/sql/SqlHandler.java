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
import io.micronaut.core.annotation.Internal;

import java.sql.SQLException;

/**
 * Interface for handling Sql annotation for different data sources.
 *
 * @param <T> The type of the data source
 *
 * @since 4.1.0
 * @author Tim Yates
 */
@Internal
@Experimental
@FunctionalInterface
public interface SqlHandler<T> {

    /**
     * Given a data source and SQL, execute the SQL.
     *
     * @param source The data source
     * @param sql The SQL to execute
     * @throws SQLException If an error occurs executing the SQL
     */
    void handle(T source, String sql) throws SQLException;
}
