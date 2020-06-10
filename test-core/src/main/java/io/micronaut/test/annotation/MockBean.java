/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.test.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.context.scope.Refreshable;
import io.micronaut.test.condition.TestActiveCondition;

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
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Bean
@Requires(condition = TestActiveCondition.class)
@Refreshable(TestActiveCondition.ACTIVE_MOCKS)
public @interface MockBean {
    /**
     * @return The bean this mock replaces
     */
    @AliasFor(annotation = Replaces.class, member = "value")
    Class value() default void.class;

    /**
     * @return The bean this mock replaces
     */
    @AliasFor(annotation = Replaces.class, member = "value")
    Class bean() default void.class;

    /**
     * The name of the bean to replace in the case of multiple beans.
     *
     * @return The qualifier
     */
    @AliasFor(annotation = Replaces.class, member = "named")
    String named() default "";
}
