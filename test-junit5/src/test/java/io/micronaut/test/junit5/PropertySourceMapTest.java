package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.annotation.Nonnull;
import java.util.Map;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PropertySourceMapTest implements TestPropertyProvider {

    @Property(name = "foo.bar")
    String val;

    @Test
    void testPropertySource() {
        Assertions.assertEquals("one", val);
    }

    @Nonnull
    @Override
    public Map<String, String> getProperties() {
        return CollectionUtils.mapOf(
                "foo.bar", "one",
                "foo.baz", "two"
        );
    }
}
