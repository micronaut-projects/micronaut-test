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

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.*;
import io.micronaut.test.annotation.MockBean;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.mockito.Mockito.*;
import javax.inject.Inject;

@MicronautTest
class MathCollaboratorTest {

    @Inject
    MathService mathService;

    @Inject
    @Client("/")
    RxHttpClient client; // <2>


    @ParameterizedTest
    @CsvSource({"2,4", "3,9"})
    void testComputeNumToSquare(Integer num, Integer square) {

        when( mathService.compute(num) )
            .then(invocation -> Long.valueOf(Math.round(Math.pow(num, 2))).intValue());

        final Integer result = client.toBlocking().retrieve(HttpRequest.GET("/math/compute/" + num), Integer.class); // <3>

        assertEquals(
                square,
                result
        );
        verify(mathService).compute(num); // <4>
    }

    @MockBean(MathServiceImpl.class) // <1>
    MathService mathService() {
        return mock(MathService.class);
    }

}
