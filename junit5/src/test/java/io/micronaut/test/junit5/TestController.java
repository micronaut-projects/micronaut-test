package io.micronaut.test.junit5;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/test")
public class TestController {


    final TestService testService;

    TestController(TestService testService) {
        this.testService = testService;
    }

    @Get
    String index() {
        return testService.doStuff();
    }
}
