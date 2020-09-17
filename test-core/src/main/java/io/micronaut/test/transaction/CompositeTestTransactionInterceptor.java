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
package io.micronaut.test.transaction;

/**
 * A test transaction interceptor.
 *
 * @author graemerocher
 */
@Deprecated
public class CompositeTestTransactionInterceptor implements TestTransactionInterceptor {
    private final TestTransactionInterceptor[] interceptors;

    /**
     * @param interceptors the interceptors
     */
    public CompositeTestTransactionInterceptor(TestTransactionInterceptor... interceptors) {
        this.interceptors = interceptors;
    }

    @Override
    public void begin() {
        for (TestTransactionInterceptor interceptor : interceptors) {
            interceptor.begin();
        }
    }

    @Override
    public void commit() {
        for (TestTransactionInterceptor interceptor : interceptors) {
            interceptor.commit();
        }
    }

    @Override
    public void rollback() {
        for (TestTransactionInterceptor interceptor : interceptors) {
            interceptor.rollback();
        }
    }
}
