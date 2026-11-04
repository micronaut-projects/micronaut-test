package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Type;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Injection points that the compiler qualifies in its own way: mocks are still unwrapped, and real
 * beans with several candidates are still injected.
 */
@Property(name = "spec.name", value = QualifiedInjectionPointMockTest.SPEC)
@MicronautTest
@DisabledInNativeImage
class QualifiedInjectionPointMockTest {

    static final String SPEC = "QualifiedInjectionPointMockTest";

    @Inject
    @Type(FirstGreeter.class)
    Greeter typeQualified;

    @Inject
    @Primary
    Mocked primaryMock;

    private Mocked fromSetter;

    @Inject
    void setFromSetter(@Primary Mocked fromSetter) {
        this.fromSetter = fromSetter;
    }

    @Test
    void typeQualifiedBeanIsInjected() {
        assertEquals("first", typeQualified.greet());
    }

    @Test
    void mockIsUnwrappedAtAPrimaryInjectionPoint() {
        assertTrue(Mockito.mockingDetails(primaryMock).isMock());
        assertTrue(Mockito.mockingDetails(fromSetter).isMock());
    }

    @MockBean(RealMocked.class)
    Mocked mocked() {
        return Mockito.mock(Mocked.class);
    }

    interface Greeter {
        String greet();
    }

    interface Mocked {
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC)
    static class FirstGreeter implements Greeter {
        @Override
        public String greet() {
            return "first";
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC)
    static class SecondGreeter implements Greeter {
        @Override
        public String greet() {
            return "second";
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC)
    static class RealMocked implements Mocked {
    }
}
