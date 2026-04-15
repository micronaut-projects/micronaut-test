package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@MicronautTest(rebuildContext = true)
@Requires(property = "mockito.test.enabled", defaultValue = StringUtils.FALSE, value = StringUtils.TRUE)
class MockBeanNestedRebuildContextTest {

    @Inject
    SomeBean someBean;

    @Nested
    class FirstNestedClass {

        @Test
        void mockIsAvailable() {
            assertNotNull(someBean);
        }
    }

    @Nested
    class SecondNestedClass {

        @Test
        void mockIsStillAvailable() {
            assertNotNull(someBean);
        }
    }

    @MockBean(SomeBean.class)
    SomeBean someBean() {
        return mock(SomeBean.class);
    }

    @Singleton
    static class SomeBean {
    }
}
