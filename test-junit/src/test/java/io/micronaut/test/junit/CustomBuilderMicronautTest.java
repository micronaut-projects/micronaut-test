
package io.micronaut.test.junit;

import io.micronaut.test.extensions.junit.annotation.MicronautTest;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Documented
@MicronautTest(contextBuilder = CustomContextBuilder.class)
public @interface CustomBuilderMicronautTest {
}
