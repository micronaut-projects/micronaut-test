
package io.micronaut.test.junit5;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MockBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import jakarta.inject.Inject;

@MicronautAndMockitoTest
class ApplicationRunTest {

    @Inject
    @Client("/")
    HttpClient client;

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
