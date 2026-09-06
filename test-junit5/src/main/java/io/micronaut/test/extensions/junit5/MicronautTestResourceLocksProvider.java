/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.test.extensions.junit5;

import io.micronaut.core.annotation.Internal;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLocksProvider;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * Serialises everything that shares a single Micronaut test {@code ApplicationContext}.
 *
 * <p>A {@code @MicronautTest} class owns one extension instance, one application context and one set
 * of singleton beans (including {@code @MockBean}s) for the whole class, and {@code @Nested} classes
 * reuse the extension instance of their outermost enclosing class. Running the methods of such a group
 * concurrently therefore races on shared mutable state. This provider hands every method - and every
 * nested container - a lock keyed on the outermost test class, so JUnit still runs different
 * {@code @MicronautTest} classes in parallel while keeping one context single-threaded.</p>
 *
 * <p>Two escape hatches exist for tests that are known to be thread-safe:</p>
 * <ul>
 *     <li>declare {@link Execution @Execution(CONCURRENT)} directly on the test class, which opts that
 *     class out;</li>
 *     <li>set {@code -Dmicronaut.test.parallel.methods=true}, which opts the whole JVM out.</li>
 * </ul>
 *
 * <p>In both cases the test class, its beans and any {@code TestExecutionListener} on its classpath
 * become the author's responsibility.</p>
 *
 * @author Denis Stepanov
 * @since 5.2.0
 */
@Internal
public final class MicronautTestResourceLocksProvider implements ResourceLocksProvider {

    /**
     * System property that disables the per-context lock for the whole JVM.
     */
    public static final String PARALLEL_METHODS_PROPERTY = "micronaut.test.parallel.methods";

    private static final String KEY_PREFIX = "micronaut.test.context:";

    @Override
    public Set<Lock> provideForNestedClass(List<Class<?>> enclosingInstanceTypes, Class<?> testClass) {
        return locksFor(enclosingInstanceTypes, testClass);
    }

    @Override
    public Set<Lock> provideForMethod(List<Class<?>> enclosingInstanceTypes, Class<?> testClass, Method testMethod) {
        return locksFor(enclosingInstanceTypes, testClass);
    }

    private static Set<Lock> locksFor(List<Class<?>> enclosingInstanceTypes, Class<?> testClass) {
        if (Boolean.getBoolean(PARALLEL_METHODS_PROPERTY)) {
            return Set.of();
        }
        Class<?> owner = enclosingInstanceTypes == null || enclosingInstanceTypes.isEmpty()
            ? testClass
            : enclosingInstanceTypes.get(0);
        if (isConcurrentByDeclaration(owner)) {
            return Set.of();
        }
        return Set.of(new Lock(KEY_PREFIX + owner.getName()));
    }

    /**
     * Whether the class itself asks for concurrent execution. Only a directly declared annotation
     * counts: an execution mode that merely comes from a configuration parameter says nothing about
     * whether the class was written to be thread-safe.
     *
     * @param testClass The outermost test class
     * @return true if the class opts out of the per-context lock
     */
    private static boolean isConcurrentByDeclaration(Class<?> testClass) {
        Execution execution = testClass.getDeclaredAnnotation(Execution.class);
        return execution != null && execution.value() == ExecutionMode.CONCURRENT;
    }
}
