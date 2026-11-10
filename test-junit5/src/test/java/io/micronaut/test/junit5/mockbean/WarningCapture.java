package io.micronaut.test.junit5.mockbean;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micronaut.test.extensions.AbstractMicronautExtension;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Captures what the Micronaut extension logs while a test class runs. Declare it with
 * {@code @ExtendWith} before {@code @MicronautTest}, so it is attached before the context starts.
 * The logger is shared by classes running in parallel, so a class only looks at the messages that
 * name it.
 */
final class WarningCapture implements BeforeAllCallback, AfterAllCallback {

    private static final Map<Class<?>, ListAppender<ILoggingEvent>> APPENDERS = new ConcurrentHashMap<>();

    @Override
    public void beforeAll(ExtensionContext context) {
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        APPENDERS.put(context.getRequiredTestClass(), appender);
        logger().addAppender(appender);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        logger().detachAppender(APPENDERS.remove(context.getRequiredTestClass()));
    }

    static void assertReported(Class<?> testClass, Class<?> bean) {
        assertReported(testClass, bean.getName());
    }

    static void assertReported(Class<?> testClass, String text) {
        assertTrue(isReported(testClass, text), () -> text + " is not among " + APPENDERS.get(testClass).list);
    }

    static void assertNotReported(Class<?> testClass, Class<?> bean) {
        assertNotReported(testClass, bean.getName());
    }

    static void assertNotReported(Class<?> testClass, String text) {
        assertFalse(isReported(testClass, text), () -> text + " is among " + APPENDERS.get(testClass).list);
    }

    private static boolean isReported(Class<?> testClass, String text) {
        return APPENDERS.get(testClass).list.stream()
            .map(ILoggingEvent::getFormattedMessage)
            .anyMatch(message -> message.contains(testClass.getName()) && message.contains(text));
    }

    private static Logger logger() {
        return (Logger) LoggerFactory.getLogger(AbstractMicronautExtension.class);
    }
}
