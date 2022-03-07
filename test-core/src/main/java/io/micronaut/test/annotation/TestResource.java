package io.micronaut.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.micronaut.core.annotation.InstantiatedMember;
import io.micronaut.test.support.resource.TestResourceManager;

/**
 * A test resource represents a resource that will be instantiated once for the scope all of tests that require the resource. That is the first test that requires the resource will instantiate and then all subsequent tests that depend on the same resource will use that resource.
 *
 * @author graemerocher
 * @since 3.1.0
 */
@Target(ElementType.TYPE)
@Repeatable(TestResource.List.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestResource {
    /**
     * An implementation of {@link io.micronaut.test.support.resource.TestResourceManager}.
     * Must have a public no argument constructor.
     */
    @InstantiatedMember
    Class<? extends TestResourceManager> value();

    /**
     * The name member can be used to request a specific named resource of the given type. For example
     * you could request a particular version of a test container.
     *
     * @return The name of the resource.
     */
    String name() default "";

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        TestResource[] value();
    }
}
