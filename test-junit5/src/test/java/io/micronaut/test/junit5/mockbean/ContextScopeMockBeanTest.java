package io.micronaut.test.junit5.mockbean;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A mock replaces a bean declared with @Context.
 */
@MicronautTest
@Property(name = "spec.name", value = "ContextScopeMockBeanTest")
class ContextScopeMockBeanTest {

    @Inject
    ContextService contextService;

    @Test
    void theMockIsInjected() {
        assertEquals("mock", contextService.greet());
    }

    @MockBean(ContextService.class)
    ContextService mockService() {
        return new ContextService(true);
    }

    @Context
    @Requires(property = "spec.name", value = "ContextScopeMockBeanTest")
    static class ContextService {
        private final boolean mock;

        ContextService() {
            this(false);
        }

        ContextService(boolean mock) {
            this.mock = mock;
        }

        public String greet() {
            return mock ? "mock" : "real";
        }
    }
}
