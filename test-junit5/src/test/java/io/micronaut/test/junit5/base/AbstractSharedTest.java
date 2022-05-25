
package io.micronaut.test.junit5.base;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.junit5.MathService;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractSharedTest {

    @Inject
    MathService mathService;
}
