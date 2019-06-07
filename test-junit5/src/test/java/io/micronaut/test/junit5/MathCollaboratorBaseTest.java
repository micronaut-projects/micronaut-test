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
