package io.micronaut.test.junit5;

import io.micronaut.test.annotation.MockBean;

import static org.mockito.Mockito.mock;

abstract class MathBaseTest {

    @MockBean(MathServiceImpl.class)
    MathService mathService() {
        return mock(MathService.class);
    }

}
