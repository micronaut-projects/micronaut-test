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
package io.micronaut.test.extensions.junit5.annotation;

import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.annotation.Executable;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.annotation.TransactionMode;
import io.micronaut.test.condition.TestActiveCondition;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be applied to any JUnit 5 test to make it a Micronaut test.
 *
 * @author graemerocher
 * @author Álvaro Sánchez-Mariscal
 * @since 2.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@ExtendWith(MicronautJunit5Extension.class)
@Factory
@Inherited
@Requires(condition = TestActiveCondition.class)
@Executable
public @interface MicronautTest {
    /**
     * @return The application class of the application
     */
    Class<?> application() default void.class;

    /**
     * @return The environments to use.
     */
    String[] environments() default {};

    /**
     * @return The packages to consider for scanning.
     */
    String[] packages() default {};

    /**
     * One or many references to classpath. For example: "classpath:mytest.yml"
     *
     * @return The property sources
     */
    String[] propertySources() default {};

    /**
     * Whether to rollback (if possible) any data access code between each test execution.
     *
     * @return True if changes should be rolled back
     */
    boolean rollback() default true;

    /**
     * Allow disabling or enabling of automatic transaction wrapping.
     * @return Whether to wrap a test in a transaction.
     */
    boolean transactional() default true;

    /**
     * Whether to rebuild the application context before each test method.
     * @return true if the application context should be rebuilt for each test method
     */
    boolean rebuildContext() default false;

    /**
     * The application context builder to use to construct the context.
     * @return The builder
     */
    Class<? extends ApplicationContextBuilder>[] contextBuilder() default {};

    /**
     * The transaction mode describing how transactions should be handled for each test.
     * @return The transaction mode
     */
    TransactionMode transactionMode() default TransactionMode.SEPARATE_TRANSACTIONS;

    /**
     * <p>Whether to start {@link io.micronaut.runtime.EmbeddedApplication}.</p>
     *
     * <p>When false, only the application context will be started.
     * This can be used to disable {@link io.micronaut.runtime.server.EmbeddedServer}.</p>
     *
     * @return true if {@link io.micronaut.runtime.EmbeddedApplication} should be started
     */
    boolean startApplication() default true;

    /**
     * By default, with JUnit 5 the test method parameters will be resolved to beans if possible.
     * This behaviour can be problematic if in combination with the {@code ParameterizedTest} annotation.
     * Setting this member to {@code false} will completely disable bean resolution for method parameters.
     * <p>
     *
     * @return Whether to resolve test method parameters as beans.
     */
    boolean resolveParameters() default true;
}
