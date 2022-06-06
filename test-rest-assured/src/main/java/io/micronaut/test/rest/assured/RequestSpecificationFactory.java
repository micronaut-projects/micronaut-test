package io.micronaut.test.rest.assured;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.context.scope.Refreshable;
import io.micronaut.runtime.server.EmbeddedServer;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

/**
 * Helper class making it easier to create request specifications.
 *
 * @author gkrocher
 * @since 3.4.0 
 */
@Factory
@Requires(beans=EmbeddedServer.class) 
public class RequestSpecificationFactory {

    /**
     * @return A request specification for the current server.
     */
    @Refreshable
    @Requires(beans=EmbeddedServer.class) 
    RequestSpecification requestSpecification(EmbeddedServer embeddedServer) {
        return RestAssured.given()
            .port(embeddedServer.getPort());
    }

}
