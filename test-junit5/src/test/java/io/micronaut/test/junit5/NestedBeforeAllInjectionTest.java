package io.micronaut.test.junit5;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A PER_CLASS {@code @Nested} class can use the enclosing class's injected fields in its
 * {@code @BeforeAll} method, which runs before any {@code @BeforeEach}.
 */
@MicronautTest
class NestedBeforeAllInjectionTest {

    @Inject
    MathService mathService;

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class PerClassNested {

        Integer computedInBeforeAll;

        @BeforeAll
        void computeOnce() {
            assertNotNull(mathService, "the enclosing instance is injected before @BeforeAll");
            computedInBeforeAll = mathService.compute(2);
        }

        @Test
        void usesTheValueFromBeforeAll() {
            assertEquals(8, computedInBeforeAll);
        }
    }
}
