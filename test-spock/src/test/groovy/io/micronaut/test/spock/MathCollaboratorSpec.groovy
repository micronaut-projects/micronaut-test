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
package io.micronaut.test.spock

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Inject

@MicronautTest
class MathCollaboratorSpec extends Specification {

    @Inject
    MathService mathService // <2>

    @Inject
    @Client('/')
    RxHttpClient client // <3>

    @Unroll
    void "should compute #num to #square"() {
        when:
        Integer result = client.toBlocking().retrieve(HttpRequest.GET('/math/compute/10'), Integer) // <3>

        then:
        1 * mathService.compute(10) >> Math.pow(num, 2)  // <4>
        result == square

        where:
        num | square
        2   | 4
        3   | 9
    }

    @MockBean(MathServiceImpl) // <1>
    MathService mathService() {
        Mock(MathService)
    }

}
