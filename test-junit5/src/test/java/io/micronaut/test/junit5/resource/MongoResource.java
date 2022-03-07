package io.micronaut.test.junit5.resource;

import io.micronaut.test.support.resource.TestResourceDefinition;
import io.micronaut.test.support.resource.TestResourceManager;
import io.micronaut.test.support.resource.TestRun;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MongoResource implements TestResourceManager { // <1>
    private MongoDBContainer mongoDBContainer;

    @Override
    public void start(TestResourceDefinition definition, TestRun testRun) throws Exception {
        final DockerImageName imageName =
                DockerImageName.parse(definition.getName().orElse("mongo:4.0.10"));
        this.mongoDBContainer = new MongoDBContainer(imageName);
        this.mongoDBContainer.start(); // <2>
        testRun.addProperty(
                "mongodb.uri", mongoDBContainer.getReplicaSetUrl()
        ); // <3>

    }

    @Override
    public void close() {
        mongoDBContainer.stop(); // <4>
    }
}
