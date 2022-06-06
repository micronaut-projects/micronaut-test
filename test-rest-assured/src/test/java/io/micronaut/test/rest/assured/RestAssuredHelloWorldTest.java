package io.micronaut.test.rest.assured;

import static org.hamcrest.CoreMatchers.is;
import org.junit.jupiter.api.Test;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;

@MicronautTest 
public class RestAssuredHelloWorldTest {
    @Test
    void testHelloWorld(RequestSpecification spec) {
        spec
            .when().get("/hello/world")
            .then().statusCode(200)
                .body(is("Hello World"));
    }
}
