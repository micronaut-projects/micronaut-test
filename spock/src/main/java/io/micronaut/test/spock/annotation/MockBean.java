package io.micronaut.test.spock.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Bean
@Requires(condition = SpecActiveCondition.class)
public @interface MockBean {
    @AliasFor(annotation = Replaces.class, member = "value")
    Class value() default void.class;
}
