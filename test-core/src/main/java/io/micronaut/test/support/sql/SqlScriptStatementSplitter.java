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

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Splits SQL scripts into executable statements.
 *
 * @since 4.9.0
 */
@Internal
final class SqlScriptStatementSplitter {

    private SqlScriptStatementSplitter() {
    }

    static @NonNull List<String> split(@NonNull String script) {
        String trimmedScript = script.trim();
        if (trimmedScript.isEmpty()) {
            return List.of();
        }
        if (looksLikePlSqlBlock(trimmedScript)) {
            return List.of(trimmedScript);
        }

        List<String> statements = new ArrayList<>();
        StringBuilder currentStatement = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;

        for (int i = 0; i < script.length(); i++) {
            char current = script.charAt(i);
            char next = i + 1 < script.length() ? script.charAt(i + 1) : '\0';

            if (inLineComment) {
                currentStatement.append(current);
                if (current == '\n' || current == '\r') {
                    inLineComment = false;
                }
                continue;
            }
            if (inBlockComment) {
                currentStatement.append(current);
                if (current == '*' && next == '/') {
                    currentStatement.append(next);
                    i++;
                    inBlockComment = false;
                }
                continue;
            }
            if (inSingleQuote) {
                currentStatement.append(current);
                if (current == '\'') {
                    if (next == '\'') {
                        currentStatement.append(next);
                        i++;
                    } else {
                        inSingleQuote = false;
                    }
                }
                continue;
            }
            if (inDoubleQuote) {
                currentStatement.append(current);
                if (current == '"') {
                    if (next == '"') {
                        currentStatement.append(next);
                        i++;
                    } else {
                        inDoubleQuote = false;
                    }
                }
                continue;
            }

            if (current == '-' && next == '-') {
                currentStatement.append(current).append(next);
                i++;
                inLineComment = true;
                continue;
            }
            if (current == '/' && next == '*') {
                currentStatement.append(current).append(next);
                i++;
                inBlockComment = true;
                continue;
            }
            if (current == '\'') {
                currentStatement.append(current);
                inSingleQuote = true;
                continue;
            }
            if (current == '"') {
                currentStatement.append(current);
                inDoubleQuote = true;
                continue;
            }
            if (current == ';') {
                addStatement(statements, currentStatement);
                currentStatement.setLength(0);
                continue;
            }

            currentStatement.append(current);
        }

        addStatement(statements, currentStatement);
        return statements;
    }

    private static boolean looksLikePlSqlBlock(String script) {
        String upper = script.toUpperCase(Locale.ROOT);
        boolean startsWithBlock = upper.startsWith("BEGIN") || upper.startsWith("DECLARE");
        return startsWithBlock && upper.matches("(?s).*\\bEND\\s*;?\\s*$");
    }

    private static void addStatement(List<String> statements, StringBuilder currentStatement) {
        String statement = currentStatement.toString().trim();
        if (!statement.isEmpty()) {
            statements.add(statement);
        }
    }
}
