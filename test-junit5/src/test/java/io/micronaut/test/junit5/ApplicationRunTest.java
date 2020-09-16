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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import javax.inject.Inject;

@MicronautAndMockitoTest
class ApplicationRunTest {

    @Inject
    @Client("/")
    RxHttpClient client;

    @Inject
    TestService testService;

    @Test
    void testSomething() {
        when(testService.doStuff()).thenReturn("mocked by " + ApplicationRunTest.class.getName());
        Assertions.assertEquals(
                "mocked by " +ApplicationRunTest.class.getName(),
                client.toBlocking().retrieve(HttpRequest.GET("/test"), String.class)
        );
        verify(testService).doStuff();
    }

    @MockBean(DefaultTestService.class)
    TestService testService() {
        return mock(TestService.class);
    }

}
