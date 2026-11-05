package io.micronaut.test.junit5.mockbean;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The warning about a @MockBean without a value only names beans that could be injected in place
 * of the mock: a different qualifier or generic argument keeps the two apart.
 */
@ExtendWith(WarningCapture.class)
@MicronautTest
@Property(name = "spec.name", value = MockBeanDistinctCandidatesTest.SPEC)
class MockBeanDistinctCandidatesTest {

    static final String SPEC = "MockBeanDistinctCandidatesTest";

    @Test
    void beansWithAnotherQualifierAreNotReported() {
        WarningCapture.assertNotReported(MockBeanDistinctCandidatesTest.class, AdminHandler.class);
    }

    @Test
    void beansWithTheSameQualifierAreReported() {
        WarningCapture.assertReported(MockBeanDistinctCandidatesTest.class, AuditHandler.class);
    }

    @Test
    void beansWithAnotherGenericArgumentAreNotReported() {
        WarningCapture.assertNotReported(MockBeanDistinctCandidatesTest.class, LongRepository.class);
    }

    @Test
    void beansWithTheSameGenericArgumentAreReported() {
        WarningCapture.assertReported(MockBeanDistinctCandidatesTest.class, IntegerRepository.class);
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
