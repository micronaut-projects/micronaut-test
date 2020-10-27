
package io.micronaut.test.junit5;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
class MathInnerServiceTest {

    @Inject
    MathService mathService;


    @Test
    void testInnerMock() {
        final int result = mathService.compute(10);

        Assertions.assertEquals(
                50,
                result
        );
        Assertions.assertTrue(mathService instanceof MyService);
    }

    @MockBean(MathServiceImpl.class)
    static class MyService implements MathService {

        @Override
        public Integer compute(Integer num) {
            return 50;
        }
    }
}
