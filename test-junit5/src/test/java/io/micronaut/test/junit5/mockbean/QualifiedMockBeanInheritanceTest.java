package io.micronaut.test.junit5.mockbean;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A mock replacing a qualified bean should be found through the qualifier of the bean it replaces,
 * without repeating the qualifier on the mock. See QualifiedMockBeanTest for what works today, and
 * for the beans shared with this test.
 */
@Disabled("https://github.com/micronaut-projects/micronaut-test/issues/1522: a replacement does not take on the qualifiers of the bean it replaces, which needs a change in micronaut-core: https://github.com/micronaut-projects/micronaut-core/issues/14231")
@MicronautTest
@Property(name = "spec.name", value = QualifiedMockBeanTest.SPEC)
class QualifiedMockBeanInheritanceTest {

    @Inject
    BeanContext beanContext;

    @Test
    void qualifiedInjectionPointGetsTheMock() {
        assertEquals("mock", beanContext.getBean(QualifiedMockBeanTest.PlainConsumer.class).handle());
    }

    @MockBean(QualifiedMockBeanTest.PlainHandler.class)
    QualifiedMockBeanTest.PlainHandler plainHandler() {
        return new QualifiedMockBeanTest.PlainHandler() {
            @Override
            public String handle() {
                return "mock";
            }
        };
    }
}
