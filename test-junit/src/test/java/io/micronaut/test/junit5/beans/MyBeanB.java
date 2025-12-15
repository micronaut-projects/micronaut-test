
package io.micronaut.test.junit5.beans;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
@Named("B")
public class MyBeanB implements MyInterface {
    @Override
    public String test() {
        return "B";
    }
}
