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
package io.micronaut.test.junit5;

import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.inject.Inject;

import static org.junit.Assert.assertFalse;

@MicronautTest(embeddedApplication = false, rebuildContext = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DisableEmbeddedApplicationTest {

    @Inject
    private EmbeddedApplication<?> embeddedApplication;

    @Test
    @Order(1)
    void embeddedApplicationIsNotStartedWhenContextIsStarted() {
        assertFalse(embeddedApplication.isRunning());
    }

    @Test
    @Order(2)
    void embeddedApplicationIsNotStartedWhenContextIsRebuilt() {
        assertFalse(embeddedApplication.isRunning());
    }
}
