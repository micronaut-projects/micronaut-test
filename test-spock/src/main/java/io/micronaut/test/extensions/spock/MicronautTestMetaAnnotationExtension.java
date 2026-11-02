/*
 * Copyright 2017-2023 original authors
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
package io.micronaut.test.extensions.spock;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.test.extensions.spock.annotation.MicronautTest;
import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.runtime.model.SpecInfo;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * Applies {@link MicronautSpockExtension} to specs that carry {@link MicronautTest} through a
 * meta-annotation. Spock only runs an annotation-driven extension for an annotation declared
 * directly on the spec.
 *
 * @since 5.2.1
 */
@Internal
public final class MicronautTestMetaAnnotationExtension implements IGlobalExtension {

    @Override
    public void visitSpec(SpecInfo spec) {
        Class<?> specClass = spec.getReflection();
        if (specClass.getAnnotation(MicronautTest.class) != null) {
            // Spock already applies the extension for a direct annotation
            return;
        }
        MicronautTest micronautTest = findMicronautTest(specClass);
        if (micronautTest != null) {
            MicronautSpockExtension<MicronautTest> extension = new MicronautSpockExtension<>();
            extension.visitSpecAnnotation(micronautTest, spec);
            extension.visitSpec(spec);
        }
    }

    /**
     * Finds {@link MicronautTest} declared directly on the given class or on one of its meta-annotations.
     *
     * @param type The spec class
     * @return The annotation, or null if there is none
     */
    static @Nullable MicronautTest findMicronautTest(Class<?> type) {
        MicronautTest direct = type.getAnnotation(MicronautTest.class);
        return direct != null ? direct : findMetaAnnotation(type.getAnnotations(), new HashSet<>());
    }

    private static @Nullable MicronautTest findMetaAnnotation(Annotation[] annotations, Set<Class<?>> visited) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (annotationType.getName().startsWith("java.lang.annotation.") || !visited.add(annotationType)) {
                continue;
            }
            MicronautTest micronautTest = annotationType.getAnnotation(MicronautTest.class);
            if (micronautTest == null) {
                micronautTest = findMetaAnnotation(annotationType.getAnnotations(), visited);
            }
            if (micronautTest != null) {
                return micronautTest;
            }
        }
        return null;
    }
}
