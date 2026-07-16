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
package io.micronaut.test.extensions.junit5.graal;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/**
 * Extensions for Graal testing.
 */
@TargetClass(className = "io.micronaut.test.extensions.AbstractMicronautExtension")
final class MicronautJUnit5Graal {

    @Substitute
    protected boolean isTestSuiteBeanPresent(Class<?> requiredTestClass) {
        // will have already been evaluated by JIT tests, so no need to evaluate again.
        return true;
    }
}
