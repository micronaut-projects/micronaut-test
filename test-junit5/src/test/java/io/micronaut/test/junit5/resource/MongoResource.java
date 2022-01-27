package io.micronaut.test.junit5.resource;

import io.micronaut.test.support.resource.ManagedTestResource;
import io.micronaut.test.support.resource.TestRun;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MongoResource implements ManagedTestResource { // <1>
    private MongoDBContainer mongoDBContainer;

    @Override
    public void start(TestRun testRun) {
        this.mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));
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
