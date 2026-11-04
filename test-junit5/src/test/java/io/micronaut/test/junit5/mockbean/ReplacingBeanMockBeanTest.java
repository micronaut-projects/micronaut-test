package io.micronaut.test.junit5.mockbean;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A mock can replace a bean that itself replaces an interface.
 */
@Disabled("https://github.com/micronaut-projects/micronaut-test/issues/1521: the mock and the bean it replaces, which itself replaces the interface, remove each other, which needs a change in micronaut-core: https://github.com/micronaut-projects/micronaut-core/issues/14232")
@MicronautTest
@Property(name = "spec.name", value = "ReplacingBeanMockBeanTest")
class ReplacingBeanMockBeanTest {

    @Inject
    Greeter greeter;

    @Test
    void theMockReplacesTheReplacingBean() {
        assertEquals("mock", greeter.greet());
    }

    @MockBean(ReplacingGreeter.class)
    Greeter mockGreeter() {
        return () -> "mock";
    }

    interface Greeter {
        String greet();
    }

    @Singleton
    @Requires(property = "spec.name", value = "ReplacingBeanMockBeanTest")
    static class DefaultGreeter implements Greeter {
        @Override
        public String greet() {
            return "default";
        }
    }

    @Singleton
    @Replaces(Greeter.class)
    @Requires(property = "spec.name", value = "ReplacingBeanMockBeanTest")
    static class ReplacingGreeter implements Greeter {
        @Override
        public String greet() {
            return "replacing";
        }
    }
}
