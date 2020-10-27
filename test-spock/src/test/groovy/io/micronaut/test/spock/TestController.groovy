
package io.micronaut.test.spock

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller('/test')
class TestController {

    final TestService testService

    TestController(TestService testService) {
        this.testService = testService
    }

    @Get
    String index() {
        testService.doStuff()
    }
}
