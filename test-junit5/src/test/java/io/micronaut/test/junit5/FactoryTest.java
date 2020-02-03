package io.micronaut.test.junit5;

import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.junit5.beans.FactoryBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Singleton;

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
