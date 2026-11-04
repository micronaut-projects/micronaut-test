package io.micronaut.test.junit5.mockbean;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A @MockBean without a value does not replace the concrete bean of the same type, which is still
 * injected. A warning says how to fix it.
 */
// registered before the Micronaut extension, so the appender is attached before the context starts
@ExtendWith(WarningCapture.class)
@MicronautTest
@Property(name = "spec.name", value = "MockBeanWithoutValueTest")
class MockBeanWithoutValueTest {

    @Inject
    ConcreteService service;

    @Test
    void theRealBeanIsStillInjected() {
        assertEquals("real", service.call());
    }

    @Test
    void aWarningNamesTheBeanToDeclare() {
        WarningCapture.assertReported(MockBeanWithoutValueTest.class, ConcreteService.class);
        WarningCapture.assertReported(MockBeanWithoutValueTest.class, "does not declare the bean it replaces");
        WarningCapture.assertReported(MockBeanWithoutValueTest.class, "as the value of @MockBean");
    }

    @MockBean
    ConcreteService concreteService() {
        return new ConcreteService() {
            @Override
            public String call() {
                return "mock";
            }
        };
    }

    @Singleton
    @Requires(property = "spec.name", value = "MockBeanWithoutValueTest")
    static class ConcreteService {
        public String call() {
            return "real";
        }
    }
}
