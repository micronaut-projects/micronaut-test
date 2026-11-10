package io.micronaut.test.junit5.mockbean;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A mock replacing a qualified bean declares the same qualifier, so qualified injection points find it.
 */
@MicronautTest
@Property(name = "spec.name", value = QualifiedMockBeanTest.SPEC)
class QualifiedMockBeanTest {

    static final String SPEC = "QualifiedMockBeanTest";

    @Inject
    BeanContext beanContext;

    @Test
    void qualifiedInjectionPointGetsTheMock() {
        assertEquals("mock", beanContext.getBean(PlainConsumer.class).handle());
    }

    // tag::mock[]
    @MockBean(PlainHandler.class)
    @Handler("plain") // <1>
    PlainHandler plainHandler() {
        return new PlainHandler() {
            @Override
            public String handle() {
                return "mock";
            }
        };
    }
    // end::mock[]

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @interface Handler {
        String value();
    }

    @Singleton
    @Handler("plain")
    @Requires(property = "spec.name", value = SPEC)
    static class PlainHandler {
        public String handle() {
            return "real";
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC)
    static class PlainConsumer {
        private final PlainHandler handler;

        PlainConsumer(@Handler("plain") PlainHandler handler) {
            this.handler = handler;
        }

        String handle() {
            return handler.handle();
        }
    }
}
