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
package io.micronaut.test.testcontainers;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.value.PropertyResolver;
import io.micronaut.test.support.resource.TestResourceDefinition;
import io.micronaut.test.support.resource.TestResourceManager;
import io.micronaut.test.support.resource.TestRun;
import org.testcontainers.containers.Container;
import org.testcontainers.utility.DockerImageName;

/**
 * An abstract implementation for test containers resources.
 *
 * @since 3.1.0
 * @author graemerocher
 */
public abstract class ContainerTestResource<T extends Container<T>> implements TestResourceManager {
    /**
     * @return The created container if it was started otherwise null.
     */
    protected abstract @Nullable T getContainer();

    @Override
    public void close() throws Exception {
        final Container<?> container = getContainer();
        if (container instanceof AutoCloseable) {
            ((AutoCloseable) container).close();
        }
    }

    @Override
    public final void start(TestResourceDefinition definition, TestRun testRun) throws Exception {
        final DockerImageName dockerImageName = getDockerImageName(definition);
        start(definition, testRun, dockerImageName);
    }

    /**
     * Starts a docker image.
     * @param definition The definition
     * @param testRun The test run
     * @param dockerImageName The docker image name
     */
    protected abstract void start(
            @NonNull TestResourceDefinition definition,
            @NonNull TestRun testRun,
            @NonNull DockerImageName dockerImageName);

    /**
     * Gets the docker image name.
     * @param definition The definition
     * @return The image name
     */
    @NonNull
    private DockerImageName getDockerImageName(@NonNull TestResourceDefinition definition) {
        final String imageName = definition.getName()
                .orElse(getDefaultImageName());
        return DockerImageName.parse(imageName);
    }

    /**
     * @return The default image name.
     */
    @NonNull
    protected abstract String getDefaultImageName();

}
