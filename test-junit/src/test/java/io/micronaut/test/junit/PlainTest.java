
package io.micronaut.test.junit;

import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// this test is here to ensure
// tests that are annotated with @MicronautTest
// are not impacted
public class PlainTest {

    @Test
    void testRunApplicationContext() {
        try (ApplicationContext context = ApplicationContext.run()) {
            Assertions.assertTrue(true);
        }
    }
}
