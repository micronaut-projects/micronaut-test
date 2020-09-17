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
package io.micronaut.test.junit5;

import io.micronaut.context.BeanContext;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest
public class MockApplicationListenerTest {
    @Inject
    BeanContext beanContext;

    @Test
    public void test() {
        MyApplicationListener myApplicationListener = beanContext.getBean(MyApplicationListener.class);
        Assertions.assertEquals(
                "I'm the mock bean",
                myApplicationListener.getDescription()
        );
    }

    @MockBean(MyApplicationListener.class)
    public MyApplicationListener mockBean() {
        return new MyApplicationListener("I'm the mock bean");
    }

}
