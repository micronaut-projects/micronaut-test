/*
 * Copyright 2017-2024 original authors
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

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Statement;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectionFactoryHandlerTest {

    private final ConnectionFactoryHandler handler = new ConnectionFactoryHandler();

    @Test
    void executesMultiStatementScriptsOneStatementAtATime() {
        List<String> executedStatements = new ArrayList<>();

        handler.handle(connectionFactory(executedStatements), """
            DELETE FROM foo;
            DELETE FROM bar;
            """);

        assertEquals(List.of("DELETE FROM foo", "DELETE FROM bar"), executedStatements);
    }

    private static ConnectionFactory connectionFactory(List<String> executedStatements) {
        Result result = (Result) Proxy.newProxyInstance(
            Result.class.getClassLoader(),
            new Class<?>[]{Result.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "getRowsUpdated" -> Mono.just(1L);
                case "toString" -> "RecordingResult";
                default -> throw new UnsupportedOperationException(method.getName());
            }
        );

        Connection connection = (Connection) Proxy.newProxyInstance(
            Connection.class.getClassLoader(),
            new Class<?>[]{Connection.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "createStatement" -> statement((String) args[0], executedStatements, result);
                case "toString" -> "RecordingConnection";
                default -> throw new UnsupportedOperationException(method.getName());
            }
        );

        return (ConnectionFactory) Proxy.newProxyInstance(
            ConnectionFactory.class.getClassLoader(),
            new Class<?>[]{ConnectionFactory.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "create" -> Mono.just(connection);
                case "toString" -> "RecordingConnectionFactory";
                default -> throw new UnsupportedOperationException(method.getName());
            }
        );
    }

    private static Statement statement(String sql, List<String> executedStatements, Result result) {
        return (Statement) Proxy.newProxyInstance(
            Statement.class.getClassLoader(),
            new Class<?>[]{Statement.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "execute" -> {
                    executedStatements.add(sql);
                    yield Mono.just(result);
                }
                case "toString" -> "RecordingStatement";
                default -> throw new UnsupportedOperationException(method.getName());
            }
        );
    }
}
