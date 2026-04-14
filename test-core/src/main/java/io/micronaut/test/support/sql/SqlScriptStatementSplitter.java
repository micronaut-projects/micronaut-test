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
        String upper = firstNonCommentToken(script).toUpperCase(Locale.ROOT);
        boolean startsWithBlock = upper.startsWith("BEGIN") || upper.startsWith("DECLARE");
        return startsWithBlock && script.toUpperCase(Locale.ROOT).matches("(?s).*\\bEND\\s*;?\\s*$");
    }

    /**
     * Returns the script with leading block/line comments and whitespace stripped,
     * so that PL/SQL block detection works even when a script starts with comments.
     */
    private static String firstNonCommentToken(String script) {
        int i = 0;
        int len = script.length();
        while (i < len) {
            char c = script.charAt(i);
            char next = i + 1 < len ? script.charAt(i + 1) : '\0';
            if (Character.isWhitespace(c)) {
                i++;
            } else if (c == '-' && next == '-') {
                // skip line comment
                i += 2;
                while (i < len && script.charAt(i) != '\n' && script.charAt(i) != '\r') {
                    i++;
                }
            } else if (c == '/' && next == '*') {
                // skip block comment
                i += 2;
                while (i + 1 < len && !(script.charAt(i) == '*' && script.charAt(i + 1) == '/')) {
                    i++;
                }
                i = Math.min(i + 2, len); // skip closing */
            } else {
                break;
            }
        }
        return script.substring(i);
    }

    private static void addStatement(List<String> statements, StringBuilder currentStatement) {
        String statement = currentStatement.toString().trim();
        if (!statement.isEmpty() && !isCommentOnly(statement)) {
            statements.add(statement);
        }
    }

    /**
     * Returns true if the given statement contains only whitespace or SQL comments,
     * with no actual SQL tokens.
     */
    private static boolean isCommentOnly(String statement) {
        int i = 0;
        int len = statement.length();
        while (i < len) {
            char c = statement.charAt(i);
            char next = i + 1 < len ? statement.charAt(i + 1) : '\0';
            if (Character.isWhitespace(c)) {
                i++;
            } else if (c == '-' && next == '-') {
                i += 2;
                while (i < len && statement.charAt(i) != '\n' && statement.charAt(i) != '\r') {
                    i++;
                }
            } else if (c == '/' && next == '*') {
                i += 2;
                while (i + 1 < len && !(statement.charAt(i) == '*' && statement.charAt(i + 1) == '/')) {
                    i++;
                }
                i = Math.min(i + 2, len); // skip closing */
            } else {
                return false;
            }
        }
        return true;
    }
}
