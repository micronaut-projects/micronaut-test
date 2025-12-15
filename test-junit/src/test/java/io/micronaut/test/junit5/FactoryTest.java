
package io.micronaut.test.junit5;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.junit5.beans.FactoryBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@MicronautTest
public class FactoryTest {

    @Inject FactoryBean factoryBean;

    @Singleton
    FactoryBean factoryBean() {
        return new FactoryBean();
    }

    @Test
    void testFactoryMethod() {
        Assertions.assertNotNull(factoryBean);
    }
}
