package io.micronaut.test.junit5.beans;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("A")
public class MyBeanA implements MyInterface  {
    @Override
    public String test() {
        return "A";
    }
}
