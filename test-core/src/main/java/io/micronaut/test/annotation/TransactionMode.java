/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.test.annotation;

/**
 * Describes how transactions are handled for each test.
 */
public enum TransactionMode {

    /**
     * Each setup/cleanup method is wrapped in its own transaction, separate from that of the test.
     * This transaction is always committed.
     */
    SEPARATE_TRANSACTIONS,

    /**
     * All setup methods are wrapped in the same transaction as the test.
     * Cleanup methods are wrapped in separate transactions.
     */
    SINGLE_TRANSACTION;
}
