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

import io.micronaut.core.annotation.NonNull;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Resolves a {@link DataSource} to a concrete implementation that can be used to execute SQL scripts against.
 *
 * @since 4.1.0
 * @author Tim Yates
 */
@FunctionalInterface
public interface SqlScriptProcessor {

    /**
     * Process the given SQL.
     *
     * @param sql The SQL to process
     * @throws SQLException If an error occurs
     */
    void process(@NonNull String sql) throws SQLException;
}
