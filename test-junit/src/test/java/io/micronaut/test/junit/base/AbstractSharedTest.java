
package io.micronaut.test.junit.base;

import io.micronaut.test.extensions.junit.annotation.MicronautTest;
import io.micronaut.test.junit.MathService;
import jakarta.inject.Inject;
import org.junit.jupiter.api.TestInstance;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractSharedTest {

    @Inject
    MathService mathService;
}
