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

import io.micronaut.context.annotation.DefaultImplementation;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.test.annotation.Sql;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Interface for beans handing scripts for the {@link Sql} annotation.
 *
 * @param <T> the datasource type
 *
 * @since 4.1.0
 * @author Tim Yates
 */
@DefaultImplementation(DefaultTestSqlAnnotationHandler.class)
@Experimental
public interface TestSqlAnnotationHandler<T extends DataSource> {

    void handleScript(ResourceLoader loader, Sql sql, T dataSource) throws IOException, SQLException;
}
