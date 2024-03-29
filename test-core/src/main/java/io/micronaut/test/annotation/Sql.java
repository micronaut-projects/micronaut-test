/*
 * Copyright 2017-2023 original authors
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

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.core.annotation.Experimental;

import javax.sql.DataSource;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be applied to a test scenario to execute SQL against a test database prior to the sceario being run.
 *
 * @since 4.1.0
 * @author Tim Yates
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Experimental
@Repeatable(Sql.Sqls.class)
public @interface Sql {

    /**
     * @return The SQL scripts to execute
     */
    @AliasFor(member = "scripts")
    String[] value() default {};

    /**
     * The name of the datasource to use for the SQL scripts.
     *
     * @return The datasource name
     */
    String dataSourceName() default "default";

    /**
     * The phase of the test to execute the SQL scripts.
     * @return The phase
     */
    Phase phase() default Phase.BEFORE_ALL;

    /**
     * Scripts to execute, e.g. {@code "classpath:foo.sql"}.
     * @return The SQL scripts to execute
     */
    @AliasFor(member = "value")
    String[] scripts() default {};

    /**
     * @return The type of the resource to use for the SQL scripts.
     */
    Class<?> resourceType() default DataSource.class;

    /**
     * Wrapper annotation class to allow multiple Sql annotations per test class or method.
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Inherited
    @interface Sqls {

        /**
         * @return The SQL scripts to execute
         */
        Sql[] value();
    }

    /**
     * The phase of the test to execute the SQL scripts.
     */
    enum Phase {
        /**
         * Execute the SQL before all tests.
         */
        BEFORE_ALL,

        /**
         * Execute the SQL before each test.
         */
        BEFORE_EACH,

        /**
         * Execute the SQL after all tests.
         */
        AFTER_ALL,

        /**
         * Execute the SQL after each test.
         */
        AFTER_EACH
    }
}
