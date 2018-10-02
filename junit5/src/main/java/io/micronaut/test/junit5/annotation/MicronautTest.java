package io.micronaut.test.junit5.annotation;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.junit5.extensions.RunApplicationExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@ExtendWith(RunApplicationExtension.class)
@Factory
@Requires(condition = TestActiveCondition.class)
public @interface MicronautTest {
    /**
     * @return The application class of the application
     */
    Class<?> application() default void.class;
}
