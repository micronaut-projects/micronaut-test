package io.micronaut.test.junit5.parallel;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

/**
 * Endpoint used by the embedded server fixtures.
 */
@Controller("/parallel")
@Requires(property = "spec.name", value = "EmbeddedServerFixture")
class PingController {

    @Get("/ping")
    String ping() {
        return "pong";
    }
}
