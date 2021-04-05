
package io.micronaut.test.junit5;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
class NestedTestClassesTest {

    private static int testCount;

    @Inject
    private Dependency outerDependency;

    @AfterAll
    static void checkTestCount() {
        assertEquals(3, testCount);
    }

    @Test
    void incrementTestCount() {
        testCount++;
    }

    @Nested
    class FirstNestedClass {

        @Inject
        private Dependency nestedDependency;

        @Test
        void incrementTestCount() {
            testCount++;
        }

        @Test
        void enclosingTestInstanceIsInjected() {
            assertNotNull(outerDependency);
        }

        @Nested
        class SecondNestedClass {

            @Test
            void incrementTestCount() {
                testCount++;
            }

            @Test
            void allEnclosingTestInstancesAreInjected() {
                assertNotNull(outerDependency);
                assertNotNull(nestedDependency);
            }
        }
    }

    @Singleton
    static class Dependency {}
}
