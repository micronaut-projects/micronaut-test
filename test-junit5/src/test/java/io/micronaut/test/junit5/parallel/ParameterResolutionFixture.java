package io.micronaut.test.junit5.parallel;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Value;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Parameter resolution reads the extension's {@code applicationContext} and {@code testAnnotationValue}
 * fields on the calling thread, so it has to stay correct while other methods of the class are in
 * flight.
 */
@MicronautTest
@Tag(ParallelFixtures.TAG)
@Property(name = "parallel.injected", value = "injected-value")
@Property(name = "parallel.other", value = "other-value")
class ParameterResolutionFixture {

    static final String KEY = "parameter-resolution";

    @Test
    void resolvesApplicationContext(ApplicationContext applicationContext) {
        assertEquals(1, ConcurrencyRecorder.hold(KEY, 30), ParallelFixtures.NO_OVERLAP);
        assertNotNull(applicationContext);
        assertSame(applicationContext, applicationContext.getBean(ApplicationContext.class));
    }

    @Test
    void resolvesValue(@Value("${parallel.injected}") String value) {
        assertEquals(1, ConcurrencyRecorder.hold(KEY, 30), ParallelFixtures.NO_OVERLAP);
        assertEquals("injected-value", value);
    }

    @Test
    void resolvesProperty(@Property(name = "parallel.injected") String value,
                          @Property(name = "parallel.other") String other) {
        assertEquals(1, ConcurrencyRecorder.hold(KEY, 30), ParallelFixtures.NO_OVERLAP);
        assertEquals("injected-value", value);
        assertEquals("other-value", other);
    }

    @Test
    void resolvesBean(Greeter greeter) {
        assertEquals(1, ConcurrencyRecorder.hold(KEY, 30), ParallelFixtures.NO_OVERLAP);
        assertEquals("real", greeter.greet());
    }
}
