package io.micronaut.test.spock.annotation;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.spock.RunApplicationExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@org.spockframework.runtime.extension.ExtensionAnnotation(RunApplicationExtension.class)
@Factory
@Requires(condition = SpecActiveCondition.class)
public @interface MicronautTest {
    /**
     * @return The application class
     */
    Class[] application() default {};
}
