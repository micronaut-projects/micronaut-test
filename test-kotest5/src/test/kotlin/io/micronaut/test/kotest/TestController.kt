
package io.micronaut.test.kotest

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/test")
class TestController constructor(private val testService: TestService) {

    @Get
    fun index(): String {
        return testService.doStuff()
    }
}
