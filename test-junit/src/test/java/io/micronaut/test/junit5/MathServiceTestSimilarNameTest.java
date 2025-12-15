
package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest
@Requires(property = "mockito.test.enabled", defaultValue = StringUtils.FALSE, value = StringUtils.TRUE)
public class MathServiceTestSimilarNameTest {

    @Inject
    MathService mathService;


    @Test
    void testThatSimilarlyNamedTestSuitesDontLeakMocks() {
        int num = 10;
        when(mathService.compute(num))
                .then(invocation -> num * 2); // non mock impl is * 4

        final int result = mathService.compute(num);

        Assertions.assertEquals(
                20,
                result
        );
    }

    @MockBean(MathServiceImpl.class)
    MathService mathService() {
        return mock(MathService.class);
    }
}
