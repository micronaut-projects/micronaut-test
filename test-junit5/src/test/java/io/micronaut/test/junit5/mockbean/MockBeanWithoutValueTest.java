package io.micronaut.test.junit5.mockbean;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.AbstractMicronautExtension;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A @MockBean without a value does not replace the concrete bean of the same type, which is still
 * injected. A warning says how to fix it.
 */
// registered before the Micronaut extension, so the appender is attached before the context starts
@ExtendWith(MockBeanWithoutValueTest.CaptureWarnings.class)
@MicronautTest
@Property(name = "spec.name", value = "MockBeanWithoutValueTest")
class MockBeanWithoutValueTest {

    static final ListAppender<ILoggingEvent> WARNINGS = new ListAppender<>();

    @Inject
    ConcreteService service;

    @Test
    void theRealBeanIsStillInjected() {
        assertEquals("real", service.call());
    }

    @Test
    void aWarningNamesTheBeanToDeclare() {
        assertTrue(WARNINGS.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(message -> message.contains("does not declare the bean it replaces")
                    && message.contains(ConcreteService.class.getName())
                    && message.contains("@MockBean(ConcreteService.class)")),
            () -> "no warning among " + WARNINGS.list);
    }

    @MockBean
    ConcreteService concreteService() {
        return new ConcreteService() {
            @Override
            public String call() {
                return "mock";
            }
        };
    }

    static class CaptureWarnings implements BeforeAllCallback, AfterAllCallback {
        private final Logger logger = (Logger) LoggerFactory.getLogger(AbstractMicronautExtension.class);

        @Override
        public void beforeAll(ExtensionContext context) {
            WARNINGS.start();
            logger.addAppender(WARNINGS);
        }

        @Override
        public void afterAll(ExtensionContext context) {
            logger.detachAppender(WARNINGS);
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = "MockBeanWithoutValueTest")
    static class ConcreteService {
        public String call() {
            return "real";
        }
    }
}
