
package io.micronaut.test.kotest

import jakarta.inject.Singleton

@Singleton
class DefaultTestService : TestService {

    override fun doStuff(): String {
        return "original"
    }
}
