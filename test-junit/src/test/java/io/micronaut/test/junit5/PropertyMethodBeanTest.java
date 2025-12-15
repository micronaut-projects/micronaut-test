package io.micronaut.test.junit5;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.event.ApplicationStartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(rebuildContext = true)
@TestMethodOrder(MethodOrderer.MethodName.class)
public class PropertyMethodBeanTest {

    @Inject
    ApplicationContext ctx;

    @Test
    @Property(name = "mybean.enabled", value = "true")
    void a_testBeanExists() {
        assertTrue(ctx.containsBean(MyBean.class));
    }

    @Test
    void b_testBeanDoesntExists() {
        assertFalse(ctx.containsBean(MyBean.class));
    }

    @Test
    @Property(name = "mybean.enabled", value = "true")
    void c_testBeanExists2() {
        assertTrue(ctx.containsBean(MyBean.class));
    }

    @Factory
    static class MyFactory {

        @Bean
        @Requires(property = "mybean.enabled", value = "true")
        MyBean produce() {
            return new MyBean();
        }

    }

    static class MyBean {

    }


}
