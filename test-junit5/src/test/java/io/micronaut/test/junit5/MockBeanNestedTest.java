package io.micronaut.test.junit5;

import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * Demonstrates that a {@code @MockBean} properly injects into {@code @Nested} classes when
 * {@code rebuildContext = true}.
 * <p>
 * See {@link MultipleNestedTestClassesTest} for the test that doesn't make use of
 * {@code rebuildContext}
 */
@MicronautTest(rebuildContext = true)
public class MockBeanNestedTest {

    @Inject
    SomeBean someBean;

    @MockBean(SomeBean.class)
    public SomeBean someBean() {
        return mock(SomeBean.class);
    }

    @Nested
    class FirstNestedClass {

        @Test
        void test() {
            assertNotNull(someBean);
        }
    }

    @Nested
    class SecondNestedClass {

        @Test
        void test() {
            assertNotNull(someBean);
        }
    }

    @Singleton
    public static class SomeBean {
    }
}
