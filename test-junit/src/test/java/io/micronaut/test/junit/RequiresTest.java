
package io.micronaut.test.junit;

import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit.annotation.MicronautTest;
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
