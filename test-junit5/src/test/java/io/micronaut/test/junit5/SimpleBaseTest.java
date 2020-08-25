package io.micronaut.test.junit5;

import io.micronaut.test.annotation.MockBean;

import static org.mockito.Mockito.mock;

public abstract class SimpleBaseTest {

    @MockBean(SimpleService.class)
    SimpleServiceInterface simpleService() {
        return mock(SimpleService.class);
    }

}
