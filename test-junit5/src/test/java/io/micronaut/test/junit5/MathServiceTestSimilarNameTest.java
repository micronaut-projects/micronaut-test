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

import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest
public class MathServiceTestSimilarNameTest {

    @Inject
    MathService mathService;


    @Test
    void testThatSimilarlyNamedTestSuitesDontLeakMocks() {
        int num = 10;
        when(mathService.compute(num))
                .then(invocation -> num * 2); // non mock impl is * 4

        final int result = mathService.compute(num);

        Assertions.assertEquals(
                20,
                result
        );
    }

    @MockBean(MathServiceImpl.class)
    MathService mathService() {
        return mock(MathService.class);
    }
}
