/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.test.junit5;

import io.micronaut.context.ApplicationContext;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

// this test is here to ensure
// tests that are annotated with @MicronautTest
// are not impacted
public class PlainTest {

    @Test
    void testRunApplicationContext() {
        try (ApplicationContext context = ApplicationContext.run()) {
            Assert.assertTrue(true);
        }
    }
}
