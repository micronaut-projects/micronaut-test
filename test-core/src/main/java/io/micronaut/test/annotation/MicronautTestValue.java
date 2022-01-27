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

import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Internal;

/**
 * Value object for the values from any of the MicronautTest annotations.
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 2.1.0
 */
@Internal
public class MicronautTestValue {

    private final Class<?> application;
    private final String[] environments;
    private final String[] packages;
    private final String[] propertySources;
    private final boolean rollback;
    private final boolean transactional;
    private final boolean rebuildContext;
    private final Class<? extends ApplicationContextBuilder>[] contextBuilder;
    private final TransactionMode transactionMode;
    private final boolean startApplication;
    private final boolean startTestResources;

    /**
     * Default constructor.
     *  @param application     The application class of the application
     * @param environments    The environments to use.
     * @param packages        The packages to consider for scanning.
     * @param propertySources The property sources
     * @param rollback        True if changes should be rolled back
     * @param transactional   Whether to wrap a test in a transaction.
     * @param rebuildContext  true if the application context should be rebuilt for each test method
     * @param contextBuilder  The builder
     * @param transactionMode The transaction mode
     * @param startApplication Whether the start the app
     * @param startTestResources Whether to start test resources
     */
    @Creator
    public MicronautTestValue(Class<?> application,
                              String[] environments,
                              String[] packages,
                              String[] propertySources,
                              boolean rollback,
                              boolean transactional,
                              boolean rebuildContext,
                              Class<? extends ApplicationContextBuilder>[] contextBuilder,
                              TransactionMode transactionMode,
                              boolean startApplication,
                              boolean startTestResources) {
        this.application = application;
        this.environments = environments;
        this.packages = packages;
        this.propertySources = propertySources;
        this.rollback = rollback;
        this.transactional = transactional;
        this.rebuildContext = rebuildContext;
        this.contextBuilder = contextBuilder;
        this.transactionMode = transactionMode;
        this.startApplication = startApplication;
        this.startTestResources = startTestResources;
    }

    /**
     * @return The application class of the application
     */
    public Class<?> application() {
        return application;
    }

    /**
     * @return The environments to use.
     */
    public String[] environments() {
        return environments;
    }

    /**
     * @return The packages to consider for scanning.
     */
    public String[] packages() {
        return packages;
    }

    /**
     * One or many references to classpath. For example: "classpath:mytest.yml"
     *
     * @return The property sources
     */
    public String[] propertySources() {
        return propertySources;
    }

    /**
     * Whether to rollback (if possible) any data access code between each test execution.
     *
     * @return True if changes should be rolled back
     */
    public boolean rollback() {
        return rollback;
    }

    /**
     * Allow disabling or enabling of automatic transaction wrapping.
     *
     * @return Whether to wrap a test in a transaction.
     */
    public boolean transactional() {
        return transactional;
    }

    /**
     * Whether to rebuild the application context before each test method.
     *
     * @return true if the application context should be rebuilt for each test method
     */
    public boolean rebuildContext() {
        return rebuildContext;
    }

    /**
     * The application context builder to use to construct the context.
     *
     * @return The builder
     */
    public Class<? extends ApplicationContextBuilder>[] contextBuilder() {
        return contextBuilder;
    }

    /**
     * The {@link TransactionMode}.
     *
     * @return The transaction mode
     */
    public TransactionMode transactionMode() {
        return transactionMode;
    }

    /***
     * @return Whether to start the embedded application
     */
    public boolean startApplication() {
        return startApplication;
    }

    /**
     * @return Whether to start test resources
     */
    public boolean isStartTestResources() {
        return startTestResources;
    }
}
