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

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataSourceSqlHandlerTest {

    private final DataSourceSqlHandler handler = new DataSourceSqlHandler();

    @Test
    void executesMultiStatementScriptsOneStatementAtATime() {
        List<String> executedStatements = new ArrayList<>();

        handler.handle(dataSource(executedStatements), """
            DELETE FROM foo;
            DELETE FROM bar;
            """);

        assertEquals(List.of("DELETE FROM foo", "DELETE FROM bar"), executedStatements);
    }

    @Test
    void keepsSemicolonsInsideQuotesAndComments() {
        assertEquals(List.of(
            "INSERT INTO foo(message) VALUES ('hello;world')",
            "-- a comment with a ;\nINSERT INTO foo(message) VALUES ('done')",
            "/* another ; comment */\nDELETE FROM foo WHERE message = 'done'"
        ), SqlScriptStatementSplitter.split("""
            INSERT INTO foo(message) VALUES ('hello;world');
            -- a comment with a ;
            INSERT INTO foo(message) VALUES ('done');
            /* another ; comment */
            DELETE FROM foo WHERE message = 'done';
            """));
    }

    @Test
    void leavesPlSqlBlocksIntact() {
        String block = """
            BEGIN
                DELETE FROM foo;
                DELETE FROM bar;
                COMMIT;
            END;
            """;

        assertEquals(List.of(block.trim()), SqlScriptStatementSplitter.split(block));
    }

    @Test
    void detectsPlSqlBlocksWithLeadingComments() {
        String block = """
            -- This script creates a PL/SQL block
            /* Another comment */
            BEGIN
                DELETE FROM foo;
                COMMIT;
            END;
            """;

        assertEquals(List.of(block.trim()), SqlScriptStatementSplitter.split(block));
    }

    @Test
    void ignoresTrailingCommentOnlyFragments() {
        assertEquals(List.of(
            "DELETE FROM foo",
            "DELETE FROM bar"
        ), SqlScriptStatementSplitter.split("""
            DELETE FROM foo;
            DELETE FROM bar;
            -- trailing comment only
            """));
    }

    private static DataSource dataSource(List<String> executedStatements) {
        Statement statement = (Statement) Proxy.newProxyInstance(
            Statement.class.getClassLoader(),
            new Class<?>[]{Statement.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "execute" -> {
                    String sql = (String) args[0];
                    if (sql.chars().filter(ch -> ch == ';').count() > 1) {
                        throw new SQLException("multiple statements are not supported");
                    }
                    executedStatements.add(sql);
                    yield true;
                }
                case "close" -> null;
                case "toString" -> "RecordingStatement";
                default -> throw new UnsupportedOperationException(method.getName());
            }
        );

        Connection connection = (Connection) Proxy.newProxyInstance(
            Connection.class.getClassLoader(),
            new Class<?>[]{Connection.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "createStatement" -> statement;
                case "close" -> null;
                case "toString" -> "RecordingConnection";
                default -> throw new UnsupportedOperationException(method.getName());
            }
        );

        return (DataSource) Proxy.newProxyInstance(
            DataSource.class.getClassLoader(),
            new Class<?>[]{DataSource.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "getConnection" -> connection;
                case "toString" -> "RecordingDataSource";
                default -> throw new UnsupportedOperationException(method.getName());
            }
        );
    }
}
