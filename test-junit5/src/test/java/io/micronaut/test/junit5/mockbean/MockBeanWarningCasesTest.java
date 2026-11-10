package io.micronaut.test.junit5.mockbean;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.core.annotation.Order;
import io.micronaut.runtime.context.scope.Refreshable;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The cases the warning about a @MockBean without a value has to get right, beyond the type and
 * qualifier of the mock.
 */
@ExtendWith(WarningCapture.class)
@MicronautTest
@Property(name = "spec.name", value = MockBeanWarningCasesTest.SPEC)
class MockBeanWarningCasesTest {

    static final String SPEC = "MockBeanWarningCasesTest";

    @Inject
    ApplicationContext applicationContext;

    @Test
    void mocksDeclaringTheBeanTheyReplaceAreNotReported() {
        WarningCapture.assertNotReported(MockBeanWarningCasesTest.class, ByValue.class);
        WarningCapture.assertNotReported(MockBeanWarningCasesTest.class, ByBean.class);
        WarningCapture.assertNotReported(MockBeanWarningCasesTest.class, ByName.class);
    }

    @Test
    void aPrimaryMockIsNotReported() {
        WarningCapture.assertNotReported(MockBeanWarningCasesTest.class, MockIsPrimary.class);
    }

    @Test
    void aSecondaryBeanIsNotReported() {
        WarningCapture.assertNotReported(MockBeanWarningCasesTest.class, RealIsSecondary.class);
    }

    @Test
    void aSecondaryBeanIsReportedAgainstASecondaryMock() {
        assertEquals("real", applicationContext.getBean(BothSecondary.class).value(), "the higher priority real bean is selected");
        WarningCapture.assertReported(MockBeanWarningCasesTest.class, RealBothSecondary.class);
    }

    @Test
    void aMockDeclaredOnAClassIsReportedAgainstTheTypeItExtends() {
        WarningCapture.assertReported(MockBeanWarningCasesTest.class, ExtendedByMock.class);
    }

    @Test
    void anAdvisedBeanIsReportedByItsOwnClass() {
        WarningCapture.assertReported(MockBeanWarningCasesTest.class, Advised.class);
        WarningCapture.assertNotReported(MockBeanWarningCasesTest.class, "$Intercepted");
    }

    @MockBean(ByValue.class)
    ByValue byValue() {
        return new ByValue();
    }

    @MockBean(bean = ByBean.class)
    ByBean byBean() {
        return new ByBean();
    }

    @MockBean(named = "byName")
    @Named("byName")
    ByName byName() {
        return new ByName();
    }

    @MockBean
    @Primary
    MockIsPrimary mockIsPrimary() {
        return new MockIsPrimary();
    }

    @MockBean
    RealIsSecondary realIsSecondary() {
        return new RealIsSecondary();
    }

    @MockBean
    @Secondary
    @Order(0)
    BothSecondary bothSecondary() {
        return () -> "mock";
    }

    @MockBean
    Advised advised() {
        return new Advised();
    }

    @MockBean
    static class MockOnClass extends ExtendedByMock {
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC)
    static class ByValue {
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC)
    static class ByBean {
    }

    @Singleton
    @Named("byName")
    @Requires(property = "spec.name", value = SPEC)
    static class ByName {
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC)
    static class MockIsPrimary {
    }

    @Singleton
    @Secondary
    @Requires(property = "spec.name", value = SPEC)
    static class RealIsSecondary {
    }

    interface BothSecondary {
        String value();
    }

    @Singleton
    @Secondary
    @Order(-1)
    @Requires(property = "spec.name", value = SPEC)
    static class RealBothSecondary implements BothSecondary {
        @Override
        public String value() {
            return "real";
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC)
    static class ExtendedByMock {
    }

    @Refreshable
    @Requires(property = "spec.name", value = SPEC)
    static class Advised {
    }
}
