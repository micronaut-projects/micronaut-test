
package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@MicronautTest
@Requires(property = "mockito.test.enabled", defaultValue = StringUtils.FALSE, value = StringUtils.TRUE)
class MathCollaboratorBaseTest extends MathBaseTest {

    @Inject MathService mathService;

    @Inject @Client("/")
    HttpClient client;

    @Test
    void testComputeNumToSquare() {

        when( mathService.compute(10) )
            .then(invocation -> Long.valueOf(Math.round(Math.pow(2, 2))).intValue());

        final Integer result = client.toBlocking().retrieve(HttpRequest.GET("/math/compute/10"), Integer.class);

        assertEquals((Integer) 4, result);
        verify(mathService).compute(10);
    }

}
