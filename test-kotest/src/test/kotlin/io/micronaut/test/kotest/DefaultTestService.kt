
package io.micronaut.test.kotest

import javax.inject.Singleton

@Singleton
class DefaultTestService : TestService {

    override fun doStuff(): String {
        return "original"
    }
}
