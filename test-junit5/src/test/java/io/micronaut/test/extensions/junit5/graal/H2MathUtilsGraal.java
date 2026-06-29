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
package io.micronaut.test.extensions.junit5.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/**
 * Native-image substitutions for H2 classes used by the JUnit 5 test module.
 */
@TargetClass(className = "org.h2.util.MathUtils")
final class H2MathUtilsGraal {

    @Substitute
    public static byte[] generateAlternativeSeed() {
        // H2 only uses this as a fallback seed source; keep the substitution
        // simple so native-image never has to compile the problematic path.
        long now = System.currentTimeMillis();
        long nanoTime = System.nanoTime();
        return new byte[] {
            (byte) now,
            (byte) (now >>> 8),
            (byte) (now >>> 16),
            (byte) (now >>> 24),
            (byte) (now >>> 32),
            (byte) (now >>> 40),
            (byte) (now >>> 48),
            (byte) (now >>> 56),
            (byte) nanoTime,
            (byte) (nanoTime >>> 8),
            (byte) (nanoTime >>> 16),
            (byte) (nanoTime >>> 24),
            (byte) (nanoTime >>> 32),
            (byte) (nanoTime >>> 40),
            (byte) (nanoTime >>> 48),
            (byte) (nanoTime >>> 56)
        };
    }
}
