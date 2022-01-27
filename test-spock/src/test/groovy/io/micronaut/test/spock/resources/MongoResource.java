package io.micronaut.test.spock.resources;

import java.util.Collections;
import java.util.Map;

import io.micronaut.core.value.PropertyResolver;
import io.micronaut.test.support.resource.TestResource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MongoResource implements TestResource {
    private MongoDBContainer mongoDBContainer;

    @Override
    public Map<String, Object> start(PropertyResolver environment) throws Exception {
        this.mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));
        this.mongoDBContainer.start();
        return Collections.singletonMap(
                "mongodb.uri", mongoDBContainer.getReplicaSetUrl()
        );
    }

    @Override
    public void close() throws Exception {
        mongoDBContainer.stop();
    }
}
