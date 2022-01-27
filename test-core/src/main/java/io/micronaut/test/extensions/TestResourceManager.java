/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.test.extensions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.concurrent.atomic.AtomicBoolean;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.LifeCycle;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.io.service.ServiceDefinition;
import io.micronaut.core.io.service.SoftServiceLoader;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.test.support.resource.TestResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for managing test resources.
 *
 * @author graemerocher
 * @since 3.1.0
 */
public class TestResourceManager implements LifeCycle<TestResourceManager> {
    private static final Logger LOG = LoggerFactory.getLogger(TestResourceManager.class);
    protected final List<TestResource> testResources = new ArrayList<>();
    private Map<String, Object> config = new LinkedHashMap<>();
    private AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public TestResourceManager start() {
        if (running.compareAndSet(false, true)) {

            SoftServiceLoader<TestResource> testResources = SoftServiceLoader.load(TestResource.class);
            for (ServiceDefinition<TestResource> testResource : testResources) {
                if (testResource.isPresent()) {
                    try {
                        final TestResource tr = testResource.load();
                        this.testResources.add(tr);
                    } catch (ServiceConfigurationError e) {
                        final Throwable cause = e.getCause();
                        if (!(cause instanceof NoClassDefFoundError) && !(cause instanceof ClassNotFoundException)) {
                          throw e;
                        }
                    }
                }
            }
            if (!this.testResources.isEmpty()) {
                OrderUtil.sort(this.testResources);
                final Environment resourceEnvironment = ApplicationContext.builder()
                        .environments(Environment.TEST)
                        .deduceEnvironment(false)
                        .bootstrapEnvironment(false)
                        .banner(false)
                        .build()
                        .getEnvironment();
                for (TestResource tr : this.testResources) {
                    if (tr.isEnabled(resourceEnvironment)) {
                        try {
                            config.putAll(tr.start(resourceEnvironment));
                        } catch (Exception e) {
                            throw new RuntimeException("Error starting test resource: " + e.getMessage(), e);
                        }
                    }
                }
            }
        }
        return this;
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    /**
     * @return The configuration exposed by the test resources.
     */
    public @NonNull Map<String, Object> getConfig() {
        return config;
    }

    @Override
    public TestResourceManager stop() {
        if (running.compareAndSet(true, false)) {
            if (!this.testResources.isEmpty()) {
                OrderUtil.reverseSort(this.testResources);
                for (TestResource testResource : testResources) {
                    try {
                        testResource.close();
                    } catch (Exception e) {
                        LOG.warn("Error shutting down test resource: " + e.getMessage(), e);
                    }
                }
            }
            this.testResources.clear();
        }
        return this;
    }

}
