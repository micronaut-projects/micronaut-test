package io.micronaut.test.junit5.mockbean;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * A mock replaces a bean declared with @Context.
 */
@MicronautTest
@Property(name = "spec.name", value = "ContextScopeMockBeanTest")
class ContextScopeMockBeanTest {

    @Inject
    ContextService contextService;

    @Inject
    ContextConsumer consumer;

    @Test
    void theMockIsInjected() {
        assertEquals("mock", contextService.greet());
        assertEquals("mock", consumer.greet());
    }

    @Test
    void theRealBeanIsNotCreated() {
        // the mock's proxy and the mock itself are subclasses, which also run this constructor
        assertFalse(ContextService.CONSTRUCTED.contains(ContextService.class), "the real @Context bean was created");
    }

    @MockBean(ContextService.class)
    ContextService mockService() {
        return new ContextService(true) { };
    }

    @Context
    @Requires(property = "spec.name", value = "ContextScopeMockBeanTest")
    static class ContextService {
        static final Set<Class<?>> CONSTRUCTED = ConcurrentHashMap.newKeySet();

        private final boolean mock;

        ContextService() {
            this(false);
        }

        ContextService(boolean mock) {
            this.mock = mock;
            CONSTRUCTED.add(getClass());
        }

        public String greet() {
            return mock ? "mock" : "real";
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = "ContextScopeMockBeanTest")
    static class ContextConsumer {
        private final ContextService contextService;

        ContextConsumer(ContextService contextService) {
            this.contextService = contextService;
        }

        String greet() {
            return contextService.greet();
        }
    }
}
