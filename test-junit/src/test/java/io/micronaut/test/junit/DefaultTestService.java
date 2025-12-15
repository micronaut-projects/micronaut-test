
package io.micronaut.test.junit;

import jakarta.inject.Singleton;

@Singleton
public class DefaultTestService implements TestService {
    @Override
    public String doStuff() {
        return "original";
    }
}
