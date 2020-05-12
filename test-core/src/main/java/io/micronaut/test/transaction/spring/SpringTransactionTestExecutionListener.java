/*
 * Copyright 2017-2020 original authors
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
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.test.context.TestContext;
import io.micronaut.test.context.TestExecutionListener;
import io.micronaut.test.extensions.AbstractMicronautExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Integrates Spring's transaction management if it is available.
 *
 * @author graemerocher
 * @since 1.0
 */
@Requires(classes = PlatformTransactionManager.class)
@EachBean(PlatformTransactionManager.class)
@Requires(property = AbstractMicronautExtension.TEST_TRANSACTIONAL, value = StringUtils.TRUE, defaultValue = StringUtils.TRUE)
public class SpringTransactionTestExecutionListener implements TestExecutionListener {

    private final PlatformTransactionManager transactionManager;
    private TransactionStatus tx;
    private final AtomicInteger counter = new AtomicInteger();
    private final boolean rollback;

    /**
     * @param transactionManager Spring's {@code PlatformTransactionManager}
     * @param rollback {@code true} if the transaction should be rollback
     */
    public SpringTransactionTestExecutionListener(
        PlatformTransactionManager transactionManager,
        @Property(name = AbstractMicronautExtension.TEST_ROLLBACK) boolean rollback) {

        this.transactionManager = transactionManager;
        this.rollback = rollback;

    }

    @Override
    public void afterTestExecution(TestContext testContext) {
        if (counter.decrementAndGet() == 0) {
            if (rollback) {
                transactionManager.rollback(tx);
            } else {
                transactionManager.commit(tx);
            }
        }
    }

    @Override
    public void beforeTestExecution(TestContext testContext) {
        if (counter.getAndIncrement() == 0) {
            tx = transactionManager.getTransaction(new DefaultTransactionDefinition());
        }
    }
}
