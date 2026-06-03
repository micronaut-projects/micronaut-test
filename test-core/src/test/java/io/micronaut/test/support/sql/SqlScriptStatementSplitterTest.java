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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlScriptStatementSplitterTest {

    @Test
    void splitsMultiStatementScripts() {
        assertEquals(List.of(
            "DELETE FROM foo",
            "DELETE FROM bar"
        ), SqlScriptStatementSplitter.split("""
            DELETE FROM foo;
            DELETE FROM bar;
            """));
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
    void keepsSemicolonsInsideDoubleQuotedIdentifiers() {
        assertEquals(List.of(
            "INSERT INTO \"semi;colon\"(message) VALUES ('done')",
            "DELETE FROM foo"
        ), SqlScriptStatementSplitter.split("""
            INSERT INTO "semi;colon"(message) VALUES ('done');
            DELETE FROM foo;
            """));
    }

    @Test
    void keepsSemicolonsInsidePostgresDollarQuotedBlocks() {
        String block = """
            DO
            $$
                BEGIN
                    INSERT INTO dummy_table(id, text, other_text)
                    VALUES ('1836e92e-5f78-4ce4-9d52-bd00b8643827','my text','my other text');
                END
            $$;
            """;

        assertEquals(List.of(block.trim().replaceFirst(";$", "")), SqlScriptStatementSplitter.split(block));
    }

    @Test
    void keepsSemicolonsInsidePostgresTaggedDollarQuotedBlocks() {
        assertEquals(List.of(
            "CREATE FUNCTION test_function() RETURNS void AS $body$\nBEGIN\n    DELETE FROM foo;\nEND;\n$body$ LANGUAGE plpgsql",
            "DELETE FROM bar"
        ), SqlScriptStatementSplitter.split("""
            CREATE FUNCTION test_function() RETURNS void AS $body$
            BEGIN
                DELETE FROM foo;
            END;
            $body$ LANGUAGE plpgsql;
            DELETE FROM bar;
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
}
