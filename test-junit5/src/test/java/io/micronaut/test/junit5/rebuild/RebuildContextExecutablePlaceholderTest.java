package io.micronaut.test.junit5.rebuild;

import io.micronaut.context.annotation.Executable;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A placeholder in an @Executable annotation member resolves against each rebuilt context.
 */
@Disabled("https://github.com/micronaut-projects/micronaut-test/issues/1519: the annotation metadata of a bean definition keeps resolving placeholders against the environment of the first context it was used with. The fix belongs in micronaut-core.")
@MicronautTest(rebuildContext = true)
@Property(name = "spec.name", value = "RebuildContextExecutablePlaceholderTest")
@Property(name = "app.tag", value = "class")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RebuildContextExecutablePlaceholderTest {

    @Test
    @Order(1)
    @Property(name = "app.tag", value = "one")
    void firstContext() {
        assertEquals("one", TaggedProcessor.SEEN.get(TaggedProcessor.SEEN.size() - 1));
    }

    @Test
    @Order(2)
    @Property(name = "app.tag", value = "two")
    void secondContext() {
        assertEquals("two", TaggedProcessor.SEEN.get(TaggedProcessor.SEEN.size() - 1));
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Executable(processOnStartup = true)
    @interface Tagged {
        String value();
    }

    @Singleton
    @Requires(property = "spec.name", value = "RebuildContextExecutablePlaceholderTest")
    static class TaggedService {
        @Tagged("${app.tag}")
        void run() {
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = "RebuildContextExecutablePlaceholderTest")
    static class TaggedProcessor implements ExecutableMethodProcessor<Tagged> {
        static final List<String> SEEN = new CopyOnWriteArrayList<>();

        @Override
        public <B> void process(BeanDefinition<B> beanDefinition, ExecutableMethod<B, ?> method) {
            SEEN.add(method.stringValue(Tagged.class).orElse(null));
        }
    }
}
