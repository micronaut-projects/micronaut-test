package io.micronaut.test.junit5;

import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MicronautTest
@MockitoEnabled
class MathFieldMockServiceTest {

    @MockBean(MathServiceImpl.class) // <1>
    MathService mock = mock(MathService.class); // <2>

    @Inject
    MathService mathService;

    @ParameterizedTest
    @CsvSource({"2,4", "3,9"})
    void testComputeNumToSquare(Integer num, Integer square) {

        when(mathService.compute(10))
                .then(invocation -> Long.valueOf(Math.round(Math.pow(num, 2))).intValue());

        final Integer result = mathService.compute(10);

        Assertions.assertEquals(
                square,
                result
        );
        verify(mathService).compute(10); // <4>
    }

}
