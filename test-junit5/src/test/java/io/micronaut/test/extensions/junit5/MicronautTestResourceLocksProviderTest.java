package io.micronaut.test.extensions.junit5;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.ResourceLocksProvider;
import org.junit.jupiter.api.parallel.Resources;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

// theOptOutRemovesEveryLock sets a system property that the provider reads, so these methods must
// not run alongside each other - or alongside anything else touching system properties - when this
// module's own suite is run with -PparallelMode=concurrent.
@ResourceLock(Resources.SYSTEM_PROPERTIES)
class MicronautTestResourceLocksProviderTest {

    private final MicronautTestResourceLocksProvider provider = new MicronautTestResourceLocksProvider();

    @Test
    void micronautTestDeclaresTheProvider() {
        ResourceLock resourceLock = AnnotationSupport.findAnnotation(MicronautTest.class, ResourceLock.class)
            .orElseThrow(() -> new AssertionError("@MicronautTest no longer declares a @ResourceLock"));
        assertEquals(1, resourceLock.providers().length);
        assertSame(MicronautTestResourceLocksProvider.class, resourceLock.providers()[0]);
    }

    @Test
    void classesAreNotLockedSoTheyStillRunInParallel() {
        assertTrue(provider.provideForClass(Outer.class).isEmpty());
    }

    @Test
    void methodsAreLockedOnTheirOwnClass() throws NoSuchMethodException {
        Method method = Outer.class.getDeclaredMethod("test");

        assertEquals(
            Set.of(new ResourceLocksProvider.Lock("micronaut.test.context:" + Outer.class.getName(),
                ResourceAccessMode.READ_WRITE)),
            provider.provideForMethod(List.of(), Outer.class, method));
    }

    @Test
    void nestedMethodsAreLockedOnTheOutermostClass() throws NoSuchMethodException {
        Method method = Outer.Inner.class.getDeclaredMethod("test");

        assertEquals(
            Set.of(new ResourceLocksProvider.Lock("micronaut.test.context:" + Outer.class.getName())),
            provider.provideForMethod(List.of(Outer.class), Outer.Inner.class, method));
    }

    @Test
    void nestedClassesAreLockedOnTheOutermostClass() {
        assertEquals(
            Set.of(new ResourceLocksProvider.Lock("micronaut.test.context:" + Outer.class.getName())),
            provider.provideForNestedClass(List.of(Outer.class), Outer.Inner.class));
    }

    @Test
    void theOptOutRemovesEveryLock() throws NoSuchMethodException {
        Method method = Outer.class.getDeclaredMethod("test");
        System.setProperty(MicronautTestResourceLocksProvider.PARALLEL_METHODS_PROPERTY, "true");
        try {
            assertTrue(provider.provideForMethod(List.of(), Outer.class, method).isEmpty());
            assertTrue(provider.provideForNestedClass(List.of(Outer.class), Outer.Inner.class).isEmpty());
        } finally {
            System.clearProperty(MicronautTestResourceLocksProvider.PARALLEL_METHODS_PROPERTY);
        }
    }

    @Test
    void aClassDeclaringConcurrentExecutionIsNotLocked() throws NoSuchMethodException {
        Method method = ConcurrentOuter.class.getDeclaredMethod("test");

        assertTrue(provider.provideForMethod(List.of(), ConcurrentOuter.class, method).isEmpty());
        assertTrue(provider.provideForNestedClass(List.of(ConcurrentOuter.class), Outer.Inner.class).isEmpty());
    }

    @Test
    void aClassDeclaringSameThreadExecutionIsStillLocked() throws NoSuchMethodException {
        Method method = SameThreadOuter.class.getDeclaredMethod("test");

        assertEquals(
            Set.of(new ResourceLocksProvider.Lock("micronaut.test.context:" + SameThreadOuter.class.getName())),
            provider.provideForMethod(List.of(), SameThreadOuter.class, method));
    }

    // The classes below are never executed. They exist so that the tests above have a Class and a
    // Method to hand to the provider, which is a pure function of the two; their bodies are empty
    // because nothing about them is ever run.

    @Execution(ExecutionMode.CONCURRENT)
    static class ConcurrentOuter {

        void test() {
            // A method handle for the provider, never invoked.
        }
    }

    @Execution(ExecutionMode.SAME_THREAD)
    static class SameThreadOuter {

        void test() {
            // A method handle for the provider, never invoked.
        }
    }

    static class Outer {

        void test() {
            // A method handle for the provider, never invoked.
        }

        static class Inner {

            void test() {
                // A method handle for the provider, never invoked.
            }
        }
    }
}
