package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
@Requires(property = "mockito.test.enabled", defaultValue = StringUtils.FALSE, value = StringUtils.TRUE)
public class Simple1Test extends SimpleBaseTest {

    @Inject
    SimpleService simpleService;

    @Test
    void testComputeNumToSquare() {
        assertNotNull(simpleService);
    }
}
