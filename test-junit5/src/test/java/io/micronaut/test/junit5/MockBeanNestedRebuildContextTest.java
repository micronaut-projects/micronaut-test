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
import org.junit.jupiter.api.condition.DisabledInNativeImage;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;

@MicronautTest(rebuildContext = true)
@Requires(property = "mockito.test.enabled", defaultValue = StringUtils.FALSE, value = StringUtils.TRUE)
// the @Requires above does not stop the nested classes running, and Mockito cannot mock inside a native image
@DisabledInNativeImage
class MockBeanNestedRebuildContextTest {

    @Inject
    ApplicationContext context;

    @Inject
    SomeBean someBean;

    private void assertTheMockOfTheRunningContextIsInjected() {
        assertTrue(mockingDetails(someBean).isMock(), "someBean should be a Mockito mock");
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

        @Nested
        class DeeperNestedClass {

            @Test
            void mockIsAvailableTwoLevelsDown() {
                assertTheMockOfTheRunningContextIsInjected();
            }
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
