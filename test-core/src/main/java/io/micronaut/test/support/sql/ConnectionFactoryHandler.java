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
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Handler for r2dbc {@link ConnectionFactory} instances.
 *
 * @since 4.1.0
 * @author Tim Yates
 */
@Singleton
@Internal
@Experimental
@Requires(classes = {ConnectionFactory.class})
public class ConnectionFactoryHandler implements SqlHandler<ConnectionFactory> {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionFactoryHandler.class);

    @Override
    public void handle(ConnectionFactory connectionFactory, String sql) {
        List<Long> rowsUpdated = Mono.from(connectionFactory.create())
            .flatMapMany(c -> {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{}: Executing SQL: {}", connectionFactory, sql);
                }
                return c.createStatement(sql).execute();
            })
            .flatMap(Result::getRowsUpdated)
            .collectList()
            .block();

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: Updated rows: {}", connectionFactory, rowsUpdated);
        }
    }
}
