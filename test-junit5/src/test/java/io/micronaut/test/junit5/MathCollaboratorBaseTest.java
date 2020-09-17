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

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@MicronautTest
class MathCollaboratorBaseTest extends MathBaseTest {

    @Inject MathService mathService;

    @Inject @Client("/") RxHttpClient client;

    @Test
    void testComputeNumToSquare() {

        when( mathService.compute(10) )
            .then(invocation -> Long.valueOf(Math.round(Math.pow(2, 2))).intValue());

        final Integer result = client.toBlocking().retrieve(HttpRequest.GET("/math/compute/10"), Integer.class);

        assertEquals((Integer) 4, result);
        verify(mathService).compute(10);
    }

}
