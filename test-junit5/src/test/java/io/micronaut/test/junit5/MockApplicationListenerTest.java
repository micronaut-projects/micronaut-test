package io.micronaut.test.junit5;

import io.micronaut.context.BeanContext;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
public class MockApplicationListenerTest {
    @Inject
    BeanContext beanContext;

    @Test
    public void test() {
        MyApplicationListener myApplicationListener = beanContext.getBean(MyApplicationListener.class);
        Assertions.assertEquals(
                "I'm the mock bean",
                myApplicationListener.getDescription()
        );
    }

    @MockBean(MyApplicationListener.class)
    public MyApplicationListener mockBean() {
        return new MyApplicationListener("I'm the mock bean");
    }


}
