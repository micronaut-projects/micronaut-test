/*
 * Copyright 2017-2022 original authors
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

import io.micronaut.test.extensions.TestResourceManager;
import io.micronaut.test.extensions.spock.annotation.MicronautTest;
import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.runtime.model.Tag;

/**
 * Adds support for {@link io.micronaut.test.support.resource.ManagedTestResource} for spock.
 */
public class SpockTestResourceExtension implements IGlobalExtension {
    public static final String TAG_MICRONAUT_TEST = "MicronautTest";
    public static final String KEY_TEST_RESOURCE_MANAGER = "globalConfig";
    private final TestResourceManager testResourceManager = new TestResourceManager();

    @Override
    public void visitSpec(SpecInfo spec) {
        if (spec.isAnnotationPresent(MicronautTest.class)) {
            spec.addTag(new Tag(TAG_MICRONAUT_TEST, KEY_TEST_RESOURCE_MANAGER, testResourceManager));
        }
    }

    @Override
    public void stop() {
        testResourceManager.stop();
    }
}
