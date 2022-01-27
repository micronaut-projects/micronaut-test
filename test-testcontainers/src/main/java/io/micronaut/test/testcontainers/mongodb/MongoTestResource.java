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

import io.micronaut.test.support.resource.TestRun;
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
public class MongoTestResource extends AbstractTestContainerTestResource {
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
    public void start(TestRun testRun) throws Exception {
        final DockerImageName dockerImageName = getDockerImageName(testRun.getEnvironment());
        this.container = new MongoDBContainer(dockerImageName);
        this.container.start();
        testRun.addProperty("mongodb.uri", container.getReplicaSetUrl());
    }
}
