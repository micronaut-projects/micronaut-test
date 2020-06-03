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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.inject.Inject;

import static org.mockito.Mockito.*;

@MicronautTest
class MathMockService2Test {

    @Inject
    MathService mathService; // <3>

    @Inject
    MathService[] mathServices;

    @ParameterizedTest
    @CsvSource({"2,4", "3,9"})
    void testComputeNumToSquare(Integer num, Integer square) {

        when(mathService.compute(10))
                .then(invocation -> Long.valueOf(Math.round(Math.pow(num, 2))).intValue()); // <4>

        final Integer result = mathService.compute(10);

        Assertions.assertEquals(
                square,
                result
        );

        Assertions.assertEquals(
                1,
                mathServices.length
        );
        verify(mathService).compute(10); // <4>
    }

    @MockBean(MathServiceImpl.class) // <1>
    MathService mathService() {
        return mock(MathService.class); // <2>
    }

}

