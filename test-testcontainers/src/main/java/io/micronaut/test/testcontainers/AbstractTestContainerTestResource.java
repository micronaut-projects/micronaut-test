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
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.value.PropertyResolver;
import io.micronaut.test.support.resource.TestResource;
import org.testcontainers.containers.Container;
import org.testcontainers.utility.DockerImageName;

/**
 * An abstract implementation for test containers resources.
 *
 * @since 3.1.0
 * @author graemerocher
 */
public abstract class AbstractTestContainerTestResource implements TestResource {
    /**
     * @return The created container if it was started otherwise null.
     */
    protected abstract @Nullable Container<?> getContainer();

    /**
     * @return The container type
     */
    @NonNull
    protected abstract String getContainerType();

    /**
     * @return The image label
     */
    @NonNull
    protected abstract String getImageLabel();

    /**
     * @return The image name
     */
    @NonNull
    protected abstract String getImageName();

    @Override
    public boolean isEnabled(PropertyResolver environment) {
        return ClassUtils.isPresent(getContainerType(), getClass().getClassLoader());
    }

    @Override
    public void close() throws Exception {
        final Container<?> container = getContainer();
        if (container instanceof AutoCloseable) {
            ((AutoCloseable) container).close();
        }
    }

    /**
     * Gets the docker image name.
     * @param environment The environment
     * @return The image name
     */
    @NonNull
    protected DockerImageName getDockerImageName(@NonNull PropertyResolver environment) {
        final String imageName = environment
                .getProperty("testcontainers." + getImageName() + ".image", Argument.STRING)
                .orElse(getImageName() + ":" + getImageLabel());
        return DockerImageName.parse(imageName);
    }
}
