package io.micronaut.test.rest.assured;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;

@MicronautTest
@Property(name = "micronaut.test.server.url", value = "https://jsonplaceholder.typicode.com")
class RestAssuredExternalServerTest {

    @Test
    void testSinglePost(RequestSpecification spec) {
        spec
            .when().get("/posts/1")
            .then().statusCode(200)
            .body("title", is("sunt aut facere repellat provident occaecati excepturi optio reprehenderit"));
    }

    @Test
    void testUsers(RequestSpecification spec) {
        spec
            .when().get("/users")
            .then().statusCode(200)
            .body("id", hasSize(10));
    }
}
