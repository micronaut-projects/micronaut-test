package io.micronaut.test.junit5.mockbean;

import io.micronaut.context.annotation.Requires;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A mock should stand in for a concrete bean that @Requires disables in tests. See MockOnlyBeanTest
 * for what works today.
 */
@Disabled("https://github.com/micronaut-projects/micronaut-test/issues/428: the mock's bean definition inherits the @Requires of the class it produces, so it is disabled along with that class, which needs a change in micronaut-core: https://github.com/micronaut-projects/micronaut-core/issues/14267")
@MicronautTest
class DisabledBeanMockBeanTest {

    @Inject
    MessageService messageService;

    @Test
    void theMockIsInjected() {
        assertEquals("mock", messageService.message());
    }

    @MockBean(MessageService.class)
    MessageService mockService() {
        return new MessageService() {
            @Override
            public String message() {
                return "mock";
            }
        };
    }

    @Singleton
    @Requires(property = "disabled-bean-mock.enabled", value = "true")
    static class MessageService {
        public String message() {
            return "real";
        }
    }
}
