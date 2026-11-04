package io.micronaut.test.junit5.mockbean;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A mock replaces a bean created by a @Factory method, both where it is injected directly and in
 * other beans.
 */
@MicronautTest
@Property(name = "spec.name", value = "FactoryMockBeanTest")
class FactoryMockBeanTest {

    @Inject
    Overloaded overloaded;

    @Inject
    OverloadedConsumer consumer;

    @Test
    void injectedDirectly() {
        assertEquals("mock", overloaded.call("x"));
        assertEquals("mock-array", overloaded.call(new Object[]{"x"}));
    }

    @Test
    void injectedIntoAnotherBean() {
        // the consumer goes through the generated proxy, which a test field does not after alignMocks
        assertEquals("mock", consumer.call());
        assertEquals("mock-array", consumer.callArray());
    }

    @MockBean(Overloaded.class)
    Overloaded mockOverloaded() {
        return new Overloaded() {
            @Override
            public String call(Object value) {
                return "mock";
            }

            @Override
            public String call(Object[] values) {
                return "mock-array";
            }
        };
    }

    /**
     * Has overloads that differ only in an array parameter, as in #1524.
     */
    public interface Overloaded {
        String call(Object value);

        String call(Object[] values);
    }

    @Factory
    @Requires(property = "spec.name", value = "FactoryMockBeanTest")
    static class OverloadedFactory {
        @Bean
        @Singleton
        Overloaded overloaded() {
            return new Overloaded() {
                @Override
                public String call(Object value) {
                    return "real";
                }

                @Override
                public String call(Object[] values) {
                    return "real-array";
                }
            };
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = "FactoryMockBeanTest")
    static class OverloadedConsumer {
        private final Overloaded overloaded;

        OverloadedConsumer(Overloaded overloaded) {
            this.overloaded = overloaded;
        }

        String call() {
            return overloaded.call("x");
        }

        String callArray() {
            return overloaded.call(new Object[]{"x"});
        }
    }
}
