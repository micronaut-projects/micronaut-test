
package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest(propertySources = "myprops.properties")
class PropertySourceTest {

    @Property(name = "foo.bar")
    String val;


    @Test
    void testPropertySource() {
        Assertions.assertEquals("foo", val);
    }
}

@MicronautTest(propertySources = "file:src/test/resources/io/micronaut/test/junit5/fileprops.properties")
class FilePropertySourceTest {

    @Property(name = "foo.file")
    String val;

    @Test
    void testFilePropertySource() {
        Assertions.assertEquals("file", val);
    }
}
