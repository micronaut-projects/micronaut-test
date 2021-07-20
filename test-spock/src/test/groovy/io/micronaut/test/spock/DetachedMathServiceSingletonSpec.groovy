/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.test.spock

import io.micronaut.context.annotation.*
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll
import spock.mock.DetachedMockFactory

import jakarta.inject.Inject
import jakarta.inject.Singleton

@Stepwise
@MicronautTest
@Property(name = "globalDetachedMathService", value = "true")
class DetachedMathServiceSingletonSpec extends Specification {

    @Inject
    MathService mathService

    @Unroll
    void "should compute #num to #square"() {
        when:
        def result = mathService.compute(num)

        then:
        1 * mathService.compute(_) >> { Math.pow(num, 2) }
        result == square

        where:
        num || square
        2   || 4
        3   || 9
    }

    void "call mathService once"() {
        given:
            1 * mathService.compute(_) >> 10
        when:
            def result = mathService.compute(1)

        then:
            result == 10
    }

    @Factory
    static class Mocks {
        DetachedMockFactory mockFactory = new DetachedMockFactory()

        @Bean
        @Singleton
        @Replaces(MathServiceImpl)
        @Requires(property = "globalDetachedMathService", value = "true")
        MathService mathService() {
            mockFactory.Mock(MathService)
        }
    }
}
