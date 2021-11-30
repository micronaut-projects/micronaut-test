
package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import jakarta.inject.Inject;

import static org.mockito.Mockito.*;

@MicronautTest
@Requires(property = "mockito.test.enabled", defaultValue = StringUtils.FALSE, value = StringUtils.TRUE)
class MathMockServiceTest {

    @Inject
    MathService mathService; // <3>


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

    @MockBean(MathServiceImpl.class) // <1>
    MathService mathService() {
        return mock(MathService.class); // <2>
    }

}
