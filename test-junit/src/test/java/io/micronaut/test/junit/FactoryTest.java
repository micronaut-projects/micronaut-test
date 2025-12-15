
package io.micronaut.test.junit;

import io.micronaut.test.extensions.junit.annotation.MicronautTest;
import io.micronaut.test.junit.beans.FactoryBean;
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
