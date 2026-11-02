package io.micronaut.test.junit5.meta;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@MicronautTest(transactional = false, environments = "meta")
@interface NonTransactionalMicronautTest {
}
