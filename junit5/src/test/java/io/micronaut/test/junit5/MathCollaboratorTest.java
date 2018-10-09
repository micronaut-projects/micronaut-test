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
