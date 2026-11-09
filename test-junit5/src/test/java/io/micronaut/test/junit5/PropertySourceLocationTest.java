package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Where {@code propertySources} entries are looked up: a name without a prefix is found next to
 * the test class or at the root of the classpath, and {@code classpath:} always means the root.
 */
@MicronautTest(propertySources = {"myprops.properties", "rootprops.properties", "classpath:prefixedprops.properties"})
class PropertySourceLocationTest {

    @Property(name = "foo.bar")
    String nextToTheTestClass;

    @Property(name = "foo.root")
    String atTheClasspathRoot;

    @Property(name = "foo.prefixed")
    String withClasspathPrefix;

    @Test
    void namesWithoutAPrefixAreFoundNextToTheTestOrAtTheRoot() {
        assertEquals("foo", nextToTheTestClass);
        assertEquals("root", atTheClasspathRoot);
    }

    @Test
    void classpathPrefixIsTheRoot() {
        assertEquals("prefixed", withClasspathPrefix);
    }
}
