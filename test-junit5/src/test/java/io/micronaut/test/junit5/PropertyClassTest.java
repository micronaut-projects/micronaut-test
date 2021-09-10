package io.micronaut.test.junit5;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
public class PropertyClassTest {

    @ConfigurationProperties("demo")
    static class Config {
        String foo = "It does not matter if the property value is defined here or not.";

        public String getFoo() {
            return foo;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }
    }

    @Inject
    Config config;

    @Test
    @Property(name = "demo.foo", value = "FOO")
    void testFoo() {
        assertEquals("FOO", config.getFoo());
    }
}
