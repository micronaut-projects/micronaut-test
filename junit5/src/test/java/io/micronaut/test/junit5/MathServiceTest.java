package io.micronaut.test.junit5;

import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.stubbing.Answer;

import javax.inject.Inject;

import static org.mockito.Mockito.*;

@MicronautTest
class MathServiceTest {

    @Inject
    MathService mathService;


    @ParameterizedTest
    @CsvSource({"2,4", "3,9"})
    void testComputeNumToSquare(Integer num, Integer square) {
        when(mathService.compute(10)).then((Answer<Integer>) invocation -> Long.valueOf(Math.round(Math.pow(num, 2))).intValue());
        final Integer result = mathService.compute(10);

        Assertions.assertEquals(
                square,
                result
        );
        verify(mathService).compute(10);
    }

    @MockBean(MathServiceImpl.class)
    MathService mathService() {
        return mock(MathService.class);
    }

}
