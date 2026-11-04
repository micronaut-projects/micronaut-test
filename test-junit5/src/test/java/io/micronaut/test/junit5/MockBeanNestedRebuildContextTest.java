package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.condition.DisabledInNativeImage;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;

@MicronautTest(rebuildContext = true)
@Requires(property = "mockito.test.enabled", defaultValue = StringUtils.FALSE, value = StringUtils.TRUE)
// the @Requires above does not stop the nested classes running, and Mockito cannot mock inside a native image
@DisabledInNativeImage
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class MockBeanNestedRebuildContextTest {

    @Inject
    SomeBean someBean;

    static SomeBean firstMock;

    @Nested
    @Order(1)
    class FirstNestedClass {

        @Test
        void mockIsAvailable() {
            assertNotNull(someBean);
            assertTrue(mockingDetails(someBean).isMock(), "someBean should be a Mockito mock");
            firstMock = someBean;
        }
    }

    @Nested
    @Order(2)
    class SecondNestedClass {

        @Test
        void mockIsStillAvailable() {
            assertNotNull(someBean);
            assertTrue(mockingDetails(someBean).isMock(), "someBean should be a Mockito mock");
            assertNotNull(firstMock, "FirstNestedClass should have run first and recorded its mock");
            assertNotSame(firstMock, someBean, "someBean should be a fresh mock after context rebuild, not a stale proxy");
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
