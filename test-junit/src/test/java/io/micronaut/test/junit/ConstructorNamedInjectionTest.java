
package io.micronaut.test.junit;

import io.micronaut.test.extensions.junit.annotation.MicronautTest;
import io.micronaut.test.junit.beans.MyInterface;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Named;

@MicronautTest
public class ConstructorNamedInjectionTest {

    private MyInterface myInterface;

    public ConstructorNamedInjectionTest(
            @Named("B") MyInterface myInterface) {
        this.myInterface = myInterface;
    }

    @Test
    void testConstructorInjected() {
        final String result = myInterface.test();

        Assertions.assertEquals("B", result);
    }
}
