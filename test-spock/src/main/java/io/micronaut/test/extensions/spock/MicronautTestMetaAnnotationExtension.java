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
import io.micronaut.test.annotation.AnnotationUtils;
import io.micronaut.test.extensions.spock.annotation.MicronautTest;
import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.runtime.model.SpecInfo;

/**
 * Applies {@link MicronautSpockExtension} to specs that carry {@link MicronautTest} through a
 * meta-annotation. Spock only runs an annotation-driven extension for an annotation declared
 * directly on the spec.
 *
 * <p>Spock runs global extensions before annotation-driven ones and in the order their service
 * files are found on the classpath, so the interceptors of a meta-annotated spec can sit outside
 * those of another global extension, where those of a direct {@link MicronautTest} are always
 * inside them.
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
        AnnotationUtils.findAnnotation(specClass, MicronautTest.class).ifPresent(micronautTest -> {
            MicronautSpockExtension<MicronautTest> extension = new MicronautSpockExtension<>();
            extension.visitSpecAnnotation(micronautTest, spec);
            extension.visitSpec(spec);
        });
    }
}
