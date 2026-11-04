package io.micronaut.test.junit5;

import io.micronaut.aop.InterceptedProxy;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.DisabledInNativeImage;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

/**
 * The PER_CLASS outer instance is injected once before all tests and survives the rebuilds, so it
 * has to be injected again from each rebuilt context for its nested classes.
 */
@MicronautTest(rebuildContext = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Requires(property = "mockito.test.enabled", defaultValue = StringUtils.FALSE, value = StringUtils.TRUE)
// the @Requires above does not stop the nested classes running, and Mockito cannot mock inside a native image
@DisabledInNativeImage
class MockBeanNestedRebuildContextPerClassTest {

    @Inject
    ApplicationContext context;

    @Inject
    SomeBean someBean;

    private void assertTheMockOfTheRunningContextIsInjected() {
        Object current = context.getBean(SomeBean.class);
        Object target = current instanceof InterceptedProxy<?> proxy ? proxy.interceptedTarget() : current;
        assertSame(target, someBean, "someBean should be the running context's mock, not a stale one");
    }

    @Nested
    class FirstNestedClass {

        @Test
        void mockIsAvailable() {
            assertTheMockOfTheRunningContextIsInjected();
        }
    }

    @Nested
    class SecondNestedClass {

        @Test
        void mockIsStillAvailable() {
            assertTheMockOfTheRunningContextIsInjected();
        }
    }

    @MockBean(SomeBean.class)
    SomeBean mockSomeBean() {
        return mock(SomeBean.class);
    }

    @Singleton
    static class SomeBean {
    }
}
