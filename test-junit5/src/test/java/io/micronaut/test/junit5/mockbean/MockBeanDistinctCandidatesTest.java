package io.micronaut.test.junit5.mockbean;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The warning about a @MockBean without a value only names beans that could be injected in place
 * of the mock: a different qualifier or generic argument keeps the two apart.
 */
@ExtendWith(MockBeanWithoutValueTest.CaptureWarnings.class)
@MicronautTest
@Property(name = "spec.name", value = MockBeanDistinctCandidatesTest.SPEC)
class MockBeanDistinctCandidatesTest {

    static final String SPEC = "MockBeanDistinctCandidatesTest";

    @Test
    void beansWithAnotherQualifierAreNotReported() {
        assertNotReported(AdminHandler.class);
    }

    @Test
    void beansWithTheSameQualifierAreReported() {
        assertReported(AuditHandler.class);
    }

    @Test
    void beansWithAnotherGenericArgumentAreNotReported() {
        assertNotReported(LongRepository.class);
    }

    @Test
    void beansWithTheSameGenericArgumentAreReported() {
        assertReported(IntegerRepository.class);
    }

    private static void assertReported(Class<?> bean) {
        assertTrue(isReported(bean), () -> bean.getName() + " is not among " + MockBeanWithoutValueTest.WARNINGS.list);
    }

    private static void assertNotReported(Class<?> bean) {
        assertFalse(isReported(bean), () -> bean.getName() + " is among " + MockBeanWithoutValueTest.WARNINGS.list);
    }

    private static boolean isReported(Class<?> bean) {
        return MockBeanWithoutValueTest.WARNINGS.list.stream()
            .map(ILoggingEvent::getFormattedMessage)
            .anyMatch(message -> message.contains(bean.getName()));
    }

    @MockBean
    @Named("billing")
    Handler billingHandler() {
        return () -> "mock";
    }

    @MockBean
    @Named("audit")
    Handler auditHandler() {
        return () -> "mock";
    }

    @MockBean
    Repository<String> stringRepository() {
        return () -> "mock";
    }

    @MockBean
    Repository<Integer> integerRepository() {
        return () -> 0;
    }

    interface Handler {
        String handle();
    }

    interface Repository<T> {
        T find();
    }

    @Singleton
    @Named("admin")
    @Requires(property = "spec.name", value = SPEC)
    static class AdminHandler implements Handler {
        @Override
        public String handle() {
            return "admin";
        }
    }

    @Singleton
    @Named("audit")
    @Requires(property = "spec.name", value = SPEC)
    static class AuditHandler implements Handler {
        @Override
        public String handle() {
            return "audit";
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC)
    static class LongRepository implements Repository<Long> {
        @Override
        public Long find() {
            return 1L;
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC)
    static class IntegerRepository implements Repository<Integer> {
        @Override
        public Integer find() {
            return 1;
        }
    }
}
