package io.micronaut.test.junit5.beans;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("B")
public class MyBeanB implements MyInterface {
    @Override
    public String test() {
        return "B";
    }
}
