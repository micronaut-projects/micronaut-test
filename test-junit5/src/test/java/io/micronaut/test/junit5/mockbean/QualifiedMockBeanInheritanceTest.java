package io.micronaut.test.junit5.mockbean;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A mock replacing a qualified bean should be found through the qualifier of the bean it replaces,
 * without repeating the qualifier on the mock. See QualifiedMockBeanTest for what works today.
 */
@Disabled("https://github.com/micronaut-projects/micronaut-test/issues/1522: a replacement does not take on the qualifiers of the bean it replaces, which needs a change in micronaut-core.")
@MicronautTest
@Property(name = "spec.name", value = "QualifiedMockBeanInheritanceTest")
class QualifiedMockBeanInheritanceTest {

    @Inject
    BeanContext beanContext;

    @Test
    void qualifiedInjectionPointGetsTheMock() {
        assertEquals("mock", beanContext.getBean(PlainConsumer.class).handle());
    }

    @MockBean(PlainHandler.class)
    PlainHandler plainHandler() {
        return new PlainHandler() {
            @Override
            public String handle() {
                return "mock";
            }
        };
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @interface Handler {
        String value();
    }

    @Singleton
    @Handler("plain")
    @Requires(property = "spec.name", value = "QualifiedMockBeanInheritanceTest")
    static class PlainHandler {
        public String handle() {
            return "real";
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = "QualifiedMockBeanInheritanceTest")
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
