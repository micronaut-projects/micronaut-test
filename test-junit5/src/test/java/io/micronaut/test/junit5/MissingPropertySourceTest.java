package io.micronaut.test.junit5;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.AbstractMicronautExtension;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A property source that cannot be loaded is skipped with a warning; the context still starts.
 */
// registered before the Micronaut extension, so the appender is attached before the context starts
@ExtendWith(MissingPropertySourceTest.CaptureWarnings.class)
@MicronautTest(propertySources = {"does-not-exist.properties", "no-extension", "unknown.extension"})
class MissingPropertySourceTest {

    private static final ListAppender<ILoggingEvent> WARNINGS = new ListAppender<>();

    @Inject
    ApplicationContext applicationContext;

    @Test
    void theContextStartsWithoutThem() {
        assertTrue(applicationContext.isRunning());
    }

    @Test
    void aMissingFileIsReportedAsNotFound() {
        assertWarning("does-not-exist.properties", "was not found");
    }

    @Test
    void aNameWithoutAnExtensionIsReported() {
        assertWarning("no-extension", "no property source loader handles");
    }

    @Test
    void anExtensionWithoutALoaderIsReported() {
        assertWarning("unknown.extension", "no property source loader handles");
    }

    private static void assertWarning(String propertySource, String reason) {
        assertTrue(WARNINGS.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(message -> message.contains("[" + propertySource + "]")
                    && message.contains(MissingPropertySourceTest.class.getName())
                    && message.contains(reason)),
            () -> "no such warning among " + WARNINGS.list);
    }

    static class CaptureWarnings implements BeforeAllCallback, AfterAllCallback {

        @Override
        public void beforeAll(ExtensionContext context) {
            WARNINGS.start();
            logger().addAppender(WARNINGS);
        }

        @Override
        public void afterAll(ExtensionContext context) {
            logger().detachAppender(WARNINGS);
        }

        private static Logger logger() {
            return (Logger) LoggerFactory.getLogger(AbstractMicronautExtension.class);
        }
    }
}
