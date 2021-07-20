/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.test.annotation;

import io.micronaut.core.reflect.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Common annotation utilities.
 * @since 1.0.1
 */
public final class AnnotationUtils {
    private AnnotationUtils() {
    }

    /**
     * Find all <em>repeatable</em> {@linkplain Annotation annotations} of
     * {@code annotationType} that are either <em>present</em>, <em>indirectly
     * present</em>, or <em>meta-present</em> on the supplied {@link AnnotatedElement}.
     *
     * <p>This method extends the functionality of
     * {@link java.lang.reflect.AnnotatedElement#getAnnotationsByType(Class)}
     * with additional support for meta-annotations.
     *
     * <p>In addition, if the element is a class and the repeatable annotation
     * is {@link java.lang.annotation.Inherited @Inherited}, this method will
     * search on superclasses first in order to support top-down semantics.
     * The result is that this algorithm finds repeatable annotations that
     * would be <em>shadowed</em> and therefore not visible according to Java's
     * standard semantics for inherited, repeatable annotations.
     *
     * <p>If the element is a class and the repeatable annotation is not
     * discovered within the class hierarchy, this method will additionally
     * search on interfaces implemented by each class in the hierarchy.
     *
     * <p>If the supplied {@code element} is {@code null}, this method simply
     * returns an empty list.
     *
     * @param element        the element to search on, potentially {@code null}
     * @param annotationType the repeatable annotation type to search for; never {@code null}
     * @param <A>            the annotation instance
     * @return the list of all such annotations found; neither {@code null} nor mutable
     * @see java.lang.annotation.Repeatable
     * @see java.lang.annotation.Inherited
     */
    public static <A extends Annotation> List<A> findRepeatableAnnotations(AnnotatedElement element,
                                                                           Class<A> annotationType) {
        if (annotationType == null) {
            throw new IllegalArgumentException("annotationType must not be null");
        }
        Repeatable repeatable = annotationType.getAnnotation(Repeatable.class);
        if (repeatable == null) {
            throw new IllegalArgumentException(annotationType.getName() + " must be @Repeatable");
        }
        Class<? extends Annotation> containerType = repeatable.value();
        boolean inherited = containerType.isAnnotationPresent(Inherited.class);

        // Short circuit the search algorithm.
        if (element == null) {
            return Collections.emptyList();
        }

        // We use a LinkedHashSet because the search algorithm may discover
        // duplicates, but we need to maintain the original order.
        Set<A> found = new LinkedHashSet<>(16);
        findRepeatableAnnotations(element, annotationType, containerType, inherited, found, new HashSet<>(16));
        // unmodifiable since returned from public, non-internal method(s)
        return Collections.unmodifiableList(new ArrayList<>(found));
    }

    private static <A extends Annotation> void findRepeatableAnnotations(AnnotatedElement element,
                                                                         Class<A> annotationType,
                                                                         Class<? extends Annotation> containerType,
                                                                         boolean inherited,
                                                                         Set<A> found,
                                                                         Set<Annotation> visited) {
        if (element instanceof Class) {
            Class<?> clazz = (Class<?>) element;

            // Recurse first in order to support top-down semantics for inherited, repeatable annotations.
            if (inherited) {
                Class<?> superclass = clazz.getSuperclass();
                if (superclass != null && superclass != Object.class) {
                    findRepeatableAnnotations(superclass, annotationType, containerType, inherited, found, visited);
                }
            }

            // Search on interfaces
            for (Class<?> ifc : clazz.getInterfaces()) {
                if (ifc != Annotation.class) {
                    findRepeatableAnnotations(ifc, annotationType, containerType, inherited, found, visited);
                }
            }
        }

        // Find annotations that are directly present or meta-present on directly present annotations.
        findRepeatableAnnotations(element.getDeclaredAnnotations(), annotationType, containerType, inherited, found, visited);

        // Find annotations that are indirectly present or meta-present on indirectly present annotations.
        findRepeatableAnnotations(element.getAnnotations(), annotationType, containerType, inherited, found, visited);
    }

    @SuppressWarnings("unchecked")
    private static <A extends Annotation> void findRepeatableAnnotations(Annotation[] candidates,
                                                                         Class<A> annotationType,
                                                                         Class<? extends Annotation> containerType,
                                                                         boolean inherited,
                                                                         Set<A> found,
                                                                         Set<Annotation> visited) {
        for (Annotation candidate : candidates) {
            Class<? extends Annotation> candidateAnnotationType = candidate.annotationType();
            if (!isInJavaLangAnnotationPackage(candidateAnnotationType) && visited.add(candidate)) {
                if (candidateAnnotationType.equals(annotationType)) { // Exact match?
                    found.add(annotationType.cast(candidate));
                } else if (candidateAnnotationType.equals(containerType)) { // Container?
                    // Note: it's not a legitimate containing annotation type if it doesn't declare
                    // a 'value' attribute that returns an array of the contained annotation type.
                    // See https://docs.oracle.com/javase/specs/jls/se8/html/jls-9.html#jls-9.6.3
                    Method method = ReflectionUtils.getMethod(containerType, "value").orElseThrow(
                            () -> new IllegalStateException(String.format(
                                    "Container annotation type '%s' must declare a 'value' attribute of type %s[].",
                                    containerType, annotationType)));

                    Annotation[] containedAnnotations = ReflectionUtils.invokeMethod(candidate, method);
                    found.addAll((Collection<? extends A>) asList(containedAnnotations));
                } else { // Otherwise search recursively through the meta-annotation hierarchy...
                    findRepeatableAnnotations(candidateAnnotationType, annotationType, containerType, inherited, found, visited);
                }
            }
        }
    }

    private static boolean isInJavaLangAnnotationPackage(Class<? extends Annotation> annotationType) {
        return (annotationType != null && annotationType.getName().startsWith("java.lang.annotation"));
    }
}
