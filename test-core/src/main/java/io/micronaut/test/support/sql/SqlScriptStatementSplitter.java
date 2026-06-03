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
        StatementParser parser = new StatementParser(script);
        while (parser.hasMore()) {
            if (parser.readNext(currentStatement)) {
                addStatement(statements, currentStatement);
                currentStatement.setLength(0);
            }
        }

        addStatement(statements, currentStatement);
        return statements;
    }

    private static boolean looksLikePlSqlBlock(String script) {
        String upper = firstNonCommentToken(script).toUpperCase(Locale.ROOT);
        boolean startsWithBlock = upper.startsWith("BEGIN") || upper.startsWith("DECLARE");
        return startsWithBlock && endsWithPlSqlBlockTerminator(script);
    }

    private static boolean endsWithPlSqlBlockTerminator(String script) {
        int index = skipTrailingWhitespace(script, script.length());
        if (index > 0 && script.charAt(index - 1) == ';') {
            index = skipTrailingWhitespace(script, index - 1);
        }
        return endsWithWord(script, index, "END");
    }

    private static int skipTrailingWhitespace(String text, int index) {
        int position = index;
        while (position > 0 && Character.isWhitespace(text.charAt(position - 1))) {
            position--;
        }
        return position;
    }

    private static boolean endsWithWord(String text, int endExclusive, String word) {
        int start = endExclusive - word.length();
        if (start < 0 || !text.regionMatches(true, start, word, 0, word.length())) {
            return false;
        }
        return start == 0 || !Character.isLetterOrDigit(text.charAt(start - 1));
    }

    /**
     * Returns the script with leading block/line comments and whitespace stripped,
     * so that PL/SQL block detection works even when a script starts with comments.
     */
    private static String firstNonCommentToken(String script) {
        int index = 0;
        while (index < script.length()) {
            int nextIndex = skipWhitespace(script, index);
            if (nextIndex != index) {
                index = nextIndex;
                continue;
            }
            nextIndex = skipComment(script, index);
            if (nextIndex == index) {
                break;
            }
            index = nextIndex;
        }
        return script.substring(index);
    }

    private static int skipWhitespace(String script, int index) {
        int position = index;
        while (position < script.length() && Character.isWhitespace(script.charAt(position))) {
            position++;
        }
        return position;
    }

    private static int skipComment(String script, int index) {
        if (startsWith(script, index, "--")) {
            return skipLineComment(script, index);
        }
        if (startsWith(script, index, "/*")) {
            return skipBlockComment(script, index);
        }
        return index;
    }

    private static int skipLineComment(String script, int index) {
        int position = index + 2;
        while (position < script.length() && !isLineBreak(script.charAt(position))) {
            position++;
        }
        return position;
    }

    private static int skipBlockComment(String script, int index) {
        int end = script.indexOf("*/", index + 2);
        return end >= 0 ? end + 2 : script.length();
    }

    private static boolean startsWith(String script, int index, String token) {
        return index + token.length() <= script.length() && script.startsWith(token, index);
    }

    private static boolean isLineBreak(char c) {
        return c == '\n' || c == '\r';
    }

    private static void addStatement(List<String> statements, StringBuilder currentStatement) {
        String statement = currentStatement.toString().trim();
        if (!statement.isEmpty() && !firstNonCommentToken(statement).isEmpty()) {
            statements.add(statement);
        }
    }

    private static final class StatementParser {
        private final String script;
        private int index;

        private StatementParser(String script) {
            this.script = script;
        }

        private boolean hasMore() {
            return index < script.length();
        }

        private boolean readNext(StringBuilder currentStatement) {
            if (startsWith(script, index, "--")) {
                appendRange(currentStatement, skipLineComment(script, index));
                return false;
            }
            if (startsWith(script, index, "/*")) {
                appendRange(currentStatement, skipBlockComment(script, index));
                return false;
            }

            char current = script.charAt(index);
            if (current == '\'' || current == '"') {
                appendQuoted(currentStatement, current);
                return false;
            }
            if (current == '$') {
                if (appendDollarQuoted(currentStatement)) {
                    return false;
                }
            }

            index++;
            if (current == ';') {
                return true;
            }

            currentStatement.append(current);
            return false;
        }

        private void appendQuoted(StringBuilder currentStatement, char quote) {
            appendRange(currentStatement, index + 1);
            while (index < script.length()) {
                char current = script.charAt(index);
                appendRange(currentStatement, index + 1);
                if (current == quote) {
                    if (index < script.length() && script.charAt(index) == quote) {
                        appendRange(currentStatement, index + 1);
                    } else {
                        return;
                    }
                }
            }
        }

        private boolean appendDollarQuoted(StringBuilder currentStatement) {
            int tagEnd = script.indexOf('$', index + 1);
            if (tagEnd < 0 || !isDollarQuoteTag(index + 1, tagEnd)) {
                return false;
            }

            String delimiter = script.substring(index, tagEnd + 1);
            int closingDelimiterIndex = script.indexOf(delimiter, tagEnd + 1);
            if (closingDelimiterIndex < 0) {
                return false;
            }

            appendRange(currentStatement, closingDelimiterIndex + delimiter.length());
            return true;
        }

        private boolean isDollarQuoteTag(int startInclusive, int endExclusive) {
            for (int i = startInclusive; i < endExclusive; i++) {
                char current = script.charAt(i);
                if (i == startInclusive && Character.isDigit(current)) {
                    return false;
                }
                if (current != '_' && !Character.isLetterOrDigit(current)) {
                    return false;
                }
            }
            return true;
        }

        private void appendRange(StringBuilder currentStatement, int endExclusive) {
            currentStatement.append(script, index, endExclusive);
            index = endExclusive;
        }
    }
}
