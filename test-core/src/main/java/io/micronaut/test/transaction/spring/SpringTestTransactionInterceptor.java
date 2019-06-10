/*
 * Copyright 2017-2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micronaut.test.transaction.spring;

import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.transaction.TestTransactionInterceptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Integrates Spring's transaction management if it is available.
 *
 * @author graemerocher
 * @since 1.0
 */
@Requires(classes = PlatformTransactionManager.class)
@EachBean(PlatformTransactionManager.class)
public class SpringTestTransactionInterceptor implements TestTransactionInterceptor {

    private final PlatformTransactionManager transactionManager;
    private TransactionStatus tx;
    private final AtomicInteger counter = new AtomicInteger();

    public SpringTestTransactionInterceptor(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public void begin() {
        if (counter.getAndIncrement() == 0) {
            tx = transactionManager.getTransaction(new DefaultTransactionDefinition());
        }
    }

    @Override
    public void commit() {
        if (counter.decrementAndGet() == 0) {
            transactionManager.commit(tx);
        }
    }

    @Override
    public void rollback() {
        if (counter.decrementAndGet() == 0) {
            transactionManager.rollback(tx);
        }
    }
}
