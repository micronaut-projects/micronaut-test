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

import io.micronaut.context.annotation.Factory
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import spock.lang.Specification
import spock.lang.Unroll
import spock.mock.DetachedMockFactory

import javax.inject.Inject

@MicronautTest
class DetachedMathServiceSpec extends Specification {


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


    @Factory
    static class Mocks {
        DetachedMockFactory mockFactory = new DetachedMockFactory()

        @MockBean(MathServiceImpl)
        MathService mathService() {
            mockFactory.Mock(MathService)
        }
    }
}
