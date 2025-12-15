
package io.micronaut.test.junit.beans;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
@Named("A")
public class MyBeanA implements MyInterface  {
    @Override
    public String test() {
        return "A";
    }
}
