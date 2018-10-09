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

@MicronautTest
@ExtendWith(MockitoExtension.class)
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
