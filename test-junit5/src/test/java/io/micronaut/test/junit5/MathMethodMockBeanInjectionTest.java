package io.micronaut.test.junit5;

import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;

@MicronautTest
@MockitoEnabled
class MathMethodMockBeanInjectionTest {

    private MathService mathService;

    @Inject
    void setMathService(MathService mathService) {
        this.mathService = mathService;
    }

    @Test
    void methodInjectedMockBeanIsTheMockitoMock() {
        Assertions.assertTrue(mockingDetails(mathService).isMock());
    }

    @MockBean(MathServiceImpl.class)
    MathService mockMathService() {
        return mock(MathService.class);
    }
}
