package io.micronaut.test.junit5;

import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MicronautTest
@MockitoEnabled
class MathMethodMockServiceTest {

    private MathService mathService;

    @Inject
    void setMathService(MathService mathService) {
        this.mathService = mathService;
    }

    @Test
    void testMethodInjectedMockBean() {
        when(mathService.compute(10)).thenReturn(4);

        Assertions.assertEquals(4, mathService.compute(10));
        verify(mathService).compute(10);
    }

    @MockBean(MathServiceImpl.class)
    MathService mockMathService() {
        return mock(MathService.class);
    }
}
