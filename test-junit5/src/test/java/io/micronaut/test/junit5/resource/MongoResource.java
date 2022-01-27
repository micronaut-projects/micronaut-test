package io.micronaut.test.junit5.resource;

import java.util.Collections;
import java.util.Map;

import io.micronaut.core.value.PropertyResolver;
import io.micronaut.test.support.resource.TestResource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MongoResource implements TestResource { // <1>
    private MongoDBContainer mongoDBContainer;

    @Override
    public Map<String, Object> start(PropertyResolver environment) throws Exception {
        this.mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));
        this.mongoDBContainer.start(); // <2>
        return Collections.singletonMap(
                "mongodb.uri", mongoDBContainer.getReplicaSetUrl()
        ); // <3>
    }

    @Override
    public void close() throws Exception {
        mongoDBContainer.stop(); // <4>
    }
}
