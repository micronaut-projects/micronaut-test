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
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.condition.TestActiveCondition;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * Annotation that can be applied to any Spock spec or JUnit 5 test to make it a Micronaut test.
 *
 * @author graemerocher
 * @author Álvaro Sánchez-Mariscal
 * @since 1.3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@ExtendWith(MicronautJunit5Extension.class)
@Factory
@Inherited
@Requires(condition = TestActiveCondition.class)
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
}
