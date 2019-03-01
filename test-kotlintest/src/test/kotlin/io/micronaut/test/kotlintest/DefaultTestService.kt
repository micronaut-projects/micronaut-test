package io.micronaut.test.kotlintest

import javax.inject.Singleton

@Singleton
class DefaultTestService : TestService {

    override fun doStuff(): String {
        return "original"
    }
}
