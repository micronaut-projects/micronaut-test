package io.micronaut.test.junit5;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@MicronautTest
class ApplicationContextArgumentInjectionTest {

    @Test
    void injectsApplicationContextIntoTestMethod(ApplicationContext applicationContext) {
        assertNotNull(applicationContext);
        assertSame(applicationContext, applicationContext.getBean(ApplicationContext.class));
    }
}
