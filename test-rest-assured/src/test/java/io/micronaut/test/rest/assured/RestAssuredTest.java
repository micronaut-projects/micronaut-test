package io.micronaut.test.rest.assured;

import static org.hamcrest.CoreMatchers.is;
import org.junit.jupiter.api.Test;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;

/**
 *
 * @author gkrocher
 */
@MicronautTest
public class RestAssuredTest {

    @Inject
    RequestSpecification fieldSpec;

    @Test
    void testRestAssured(RequestSpecification spec) {
        spec
            .when().get("/hello/world")
            .then().statusCode(200)
                .body(is("Hello World"));
    }

    @Test
    void testField1() {
        fieldSpec
            .when().get("/hello/world")
            .then().statusCode(200)
                .body(is("Hello World"));

    }

    @Test
    void testField2() {
        fieldSpec
            .when().get("/hello/world")
            .then().statusCode(200)
                .body(is("Hello World"));

    }

    @Controller("/hello")
    static class TestController {
        @Get("/world")
        @Produces(MediaType.TEXT_PLAIN)
        String hello() {
            return "Hello World";
        }
    }
 }
