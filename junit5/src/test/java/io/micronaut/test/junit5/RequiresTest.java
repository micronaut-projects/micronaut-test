package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Requires;
import io.micronaut.test.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
@Requires(property = "does.not.exist")
class RequiresTest {

    @Test
    void testNotExecuted() {
        Assertions.fail("Should never be executed");
    }
}
