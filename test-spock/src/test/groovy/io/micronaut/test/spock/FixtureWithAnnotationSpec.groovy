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

import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.extensions.spock.MicronautSpockExtension
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.SpecInfo
import spock.lang.Specification

@MicronautTest
class FixtureWithAnnotationSpec extends Specification {
    static Boolean firstTestPassed = false
    static Boolean setupFlag = false
    static Boolean cleanFlag = false
    static Boolean specFlag = false

    void setup() {
        setupFlag = true
    }

    void setupSpec() {
        specFlag = true
    }

    void cleanup() {
        cleanFlag = true
    }

    void "Setup should work"() {
        when: "fixture has been executed"
        firstTestPassed = true
        then: "setupFlag should be true"
        setupFlag
    }

    void "SetupSpec should work"() {
        when: "setupSpec has been executed"
        firstTestPassed = true
        then: "setupSpec should be true"
        specFlag
    }

    void "Cleanup should work"() {
        when: "cleanup has been executed"
        then: "cleanFlag should be true if not first test"
        firstTestPassed && cleanFlag
        cleanup:
        firstTestPassed = true
    }

    void "Cleanup should work Bis in case the first one passed first"() {
        when: "cleanup has been executed"
        then: "cleanFlag should be true if not first test"
        firstTestPassed && cleanFlag
        cleanup:
        firstTestPassed = true
    }

    def "CleanupSpec should work"() {
        given:
        SpecInfo specInfo = new SpecInfo()
        IMethodInvocation invocation = Mock()

        and:
        MicronautSpockExtension micronautSpockExtension = new MicronautSpockExtension()

        when: "cleanup spec interceptor is registered"
        micronautSpockExtension.visitSpecAnnotation((MicronautTest) null, specInfo)

        and: "interceptor is executed"
        specInfo.cleanupSpecInterceptors[0].intercept(invocation)

        then:
        1 * invocation.proceed()
    }
}
