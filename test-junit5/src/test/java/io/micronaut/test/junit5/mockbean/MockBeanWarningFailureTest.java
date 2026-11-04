package io.micronaut.test.junit5.mockbean;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The warning about a @MockBean without a value is only a diagnostic: its lookup failing on a bean
 * the test never uses must not fail the test, or stop the other mocks being checked.
 */
@ExtendWith(WarningCapture.class)
@MicronautTest
@Property(name = "spec.name", value = MockBeanWarningFailureTest.SPEC)
class MockBeanWarningFailureTest {

    static final String SPEC = "MockBeanWarningFailureTest";

    @Test
    void theOtherMocksAreStillChecked() {
        WarningCapture.assertReported(MockBeanWarningFailureTest.class, Concrete.class);
    }

    @MockBean
    Service service() {
        return new Service() { };
    }

    @MockBean
    Concrete concrete() {
        return new Concrete();
    }

    interface Service {
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC)
    static class Concrete {
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC)
    @Requires(condition = FailingCondition.class)
    static class FailingService implements Service {
    }

    public static class FailingCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context) {
            throw new IllegalStateException("cannot evaluate");
        }
    }
}
