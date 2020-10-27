
package io.micronaut.test.spock

import javax.inject.Singleton

@Singleton
class DefaultTestService implements TestService {
    @Override
    String doStuff() {
        return "orignal"
    }
}
