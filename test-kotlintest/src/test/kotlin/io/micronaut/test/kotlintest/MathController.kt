package io.micronaut.test.kotlintest

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/math")
class MathController internal constructor(internal var mathService: MathService) {

    @Get(uri = "/compute/{number}", processes = [MediaType.TEXT_PLAIN])
    internal fun compute(number: Int): String {
        return mathService.compute(number).toString()
    }
}
