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
package io.micronaut.test.extensions.kotest.annotation

import io.micronaut.context.ApplicationContextBuilder
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import io.micronaut.test.annotation.TransactionMode
import io.micronaut.test.condition.TestActiveCondition
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension
import org.junit.jupiter.api.extension.ExtendWith
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * Annotation that can be applied to any Kotest test to make it a Micronaut test.
 *
 * @author graemerocher
 * @author Álvaro Sánchez-Mariscal
 * @since 2.1.0
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@ExtendWith(MicronautJunit5Extension::class)
@Factory
@Inherited
@Requires(condition = TestActiveCondition::class)
annotation class MicronautTest(
        /**
         * @return The application class of the application
         */
        val application: KClass<*> = Unit::class,
        /**
         * @return The environments to use.
         */
        val environments: Array<String> = [],
        /**
         * @return The packages to consider for scanning.
         */
        val packages: Array<String> = [],
        /**
         * One or many references to classpath. For example: "classpath:mytest.yml"
         *
         * @return The property sources
         */
        val propertySources: Array<String> = [],
        /**
         * Whether to rollback (if possible) any data access code between each test execution.
         *
         * @return True if changes should be rolled back
         */
        val rollback: Boolean = true,
        /**
         * Allow disabling or enabling of automatic transaction wrapping.
         * @return Whether to wrap a test in a transaction.
         */
        val transactional: Boolean = true,
        /**
         * Whether to rebuild the application context before each test method.
         * @return true if the application context should be rebuilt for each test method
         */
        val rebuildContext: Boolean = false,
        /**
         * The application context builder to use to construct the context.
         * @return The builder
         */
        val contextBuilder: Array<KClass<out ApplicationContextBuilder>> = [],
        /**
         * The transaction mode describing how transactions should be handled for each test.
         * @return The transaction mode
         */
        val transactionMode: TransactionMode = TransactionMode.SEPARATE_TRANSACTIONS,
        /**
         *
         * Whether to start [io.micronaut.runtime.EmbeddedApplication].
         *
         *
         * When false, only the application context will be started.
         * This can be used to disable [io.micronaut.runtime.server.EmbeddedServer].
         *
         * @return true if [io.micronaut.runtime.EmbeddedApplication] should be started
         */
        val startApplication: Boolean = true,
)
