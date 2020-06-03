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
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class ApplicationRunAnotherSpec extends Specification {

    @Inject
    @Client("/")
    RxHttpClient client

    void "test ping server"() {
        expect:
        client.toBlocking().retrieve(HttpRequest.GET('/test'), String) == "mocked by " +ApplicationRunAnotherSpec.name
    }

    void "test ping server again"() {
        expect:
        client.toBlocking().retrieve(HttpRequest.GET('/test'), String) == "mocked by " +ApplicationRunAnotherSpec.name
    }

    @MockBean(DefaultTestService)
    TestService testService() {
        def mock = Mock(TestService)
        mock.doStuff() >> "mocked by " +ApplicationRunAnotherSpec.name
        return mock
    }
}

