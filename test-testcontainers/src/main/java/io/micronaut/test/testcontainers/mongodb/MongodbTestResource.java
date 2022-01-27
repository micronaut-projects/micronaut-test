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
package io.micronaut.test.testcontainers.mongodb;

import java.util.Collections;
import java.util.Map;

import io.micronaut.core.value.PropertyResolver;
import io.micronaut.test.testcontainers.AbstractTestContainerTestResource;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * A test resource for mongodb.
 *
 * @author graemerocher
 * @since 3.1.0
 */
public class MongodbTestResource extends AbstractTestContainerTestResource {
    private MongoDBContainer container;

    @Override
    protected Container<?> getContainer() {
        return container;
    }

    @Override
    protected String getContainerType() {
        return "org.testcontainers.containers.MongoDBContainer";
    }

    @Override
    protected String getImageLabel() {
        return "4.0.10";
    }

    @Override
    protected String getImageName() {
        return "mongo";
    }

    @Override
    public Map<String, Object> start(PropertyResolver environment) throws Exception {
        final DockerImageName dockerImageName = getDockerImageName(environment);
        this.container = new MongoDBContainer(dockerImageName);
        this.container.start();
        return Collections.singletonMap(
                "mongodb.uri", container.getReplicaSetUrl()
        );
    }
}
