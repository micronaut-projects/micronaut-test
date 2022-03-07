
package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

@MicronautTest
@Requires(property = "mockito.test.enabled", defaultValue = StringUtils.FALSE, value = StringUtils.TRUE)
class MathMockServiceNestedTest {

    private int number = 10;

    @Inject
    @Property(name = "mockito.test.enabled", defaultValue = StringUtils.FALSE)
    boolean mockitoEnabled = false;

    @Inject
    MathService mathService; // <3>

    @BeforeEach
    void setup() {
        number = 10;
    }

    @Test
    void mockIsAvailableOnTopLevel() {
        when(mathService.compute(number)).thenReturn(100);
        mathService.compute(number);
        verify(mathService).compute(10); // <4>
    }

    @Nested
    @DisplayName("Given number is 20")
    class GivenNumberIsTwenty {
        @BeforeEach
        void setup() {
            number = 20;
        }

        @Test
        void mockIsAvailableOnNestedLevel() {
            System.out.println("Mockito Enabled? = " + mockitoEnabled);
            if (mockitoEnabled) {
                when(mathService.compute(number)).thenReturn(100);
                mathService.compute(number);
                verify(mathService).compute(20); // <4>
            }
        }
    }

    @MockBean(MathServiceImpl.class) // <1>
    MathService mathService() {
        return mock(MathService.class); // <2>
    }

}
