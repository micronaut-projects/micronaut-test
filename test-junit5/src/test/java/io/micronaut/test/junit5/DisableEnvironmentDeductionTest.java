package io.micronaut.test.junit5;

import io.micronaut.context.env.Environment;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(deduceEnvironment = false)
class DisableEnvironmentDeductionTest {

    @Inject
    Environment environment;

    @Test
    void environmentDeductionPropertyIsFalse() {
        assertEquals(Boolean.FALSE, environment.getProperty(Environment.DEDUCE_ENVIRONMENT_PROPERTY, Boolean.class).orElse(null));
    }
}
