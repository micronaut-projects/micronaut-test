
package io.micronaut.test.junit5;

import jakarta.inject.Singleton;

@Singleton
public class DefaultTestService implements TestService {
    @Override
    public String doStuff() {
        return "original";
    }
}
