package io.micronaut.test.junit;

import io.micronaut.test.extensions.junit.annotation.MicronautTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@MicronautTest
public class MultipleNestedTestClassesTest {

    @Nested
    class FirstNestedClass {

        @Test
        void test() {
        }
    }

    @Nested
    class SecondNestedClass {

        @Test
        void test() {
        }
    }
}
