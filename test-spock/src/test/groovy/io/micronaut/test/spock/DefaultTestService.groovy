
package io.micronaut.test.spock

import jakarta.inject.Singleton

@Singleton
class DefaultTestService implements TestService {
    @Override
    String doStuff() {
        return "orignal"
    }
}
