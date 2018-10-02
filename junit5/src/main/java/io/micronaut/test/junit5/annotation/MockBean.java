package io.micronaut.test.junit5.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.context.scope.Refreshable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be applied on the method level in a test to define a Mock bean using Spock's mocking API.
 *
 * @author graemerocher
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Bean
@Requires(condition = TestActiveCondition.class)
@Refreshable(TestActiveCondition.ACTIVE_MOCKS)
public @interface MockBean {
    @AliasFor(annotation = Replaces.class, member = "value")
    Class value() default void.class;
}
