package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Context;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(rebuildContext = true)
public class RebuildContextTest {

    @Inject
    TestBean bean;

    @Test
    public void test() {
        assertEquals(1, TestBean.counter);
    }
}

@Context
class TestBean {
    static int counter = 0;

    public TestBean() {
        counter++;
    }
}
