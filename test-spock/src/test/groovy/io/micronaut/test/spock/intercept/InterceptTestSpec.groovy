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
package io.micronaut.test.spock.intercept

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.spock.MathService
import jakarta.inject.Inject
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll

@Stepwise
@MicronautTest
@Property(name = "InterceptTestSpec", value = "true")
class InterceptTestSpec extends Specification {

    @Inject
    MathService mathService

    @Inject
    TestInterceptor testInterceptor

    @Unroll
    void "should compute #num to #expected"() {
        when:
            def result = mathService.compute(num)

        then:
            result == expected

        where:
            num | expected
            2   | 8
            3   | 12
    }

    void "my test"() {
        expect:
            1 == 1
    }

    void "validate invocations"() {
        when:
            def calls = new ArrayList(testInterceptor.calls)
            def expected = [
                    'IN $spock_feature_0_0',
                    'IN BEFORE $spock_feature_0_0',
                    'OUT BEFORE $spock_feature_0_0',
                    'IN AFTER $spock_feature_0_0',
                    'OUT AFTER $spock_feature_0_0',
                    'IN BEFORE $spock_feature_0_0',
                    'OUT BEFORE $spock_feature_0_0',
                    'IN AFTER $spock_feature_0_0',
                    'OUT AFTER $spock_feature_0_0',
                    'OUT $spock_feature_0_0',

                    'IN $spock_feature_0_1',
                    'IN BEFORE $spock_feature_0_1',
                    'OUT BEFORE $spock_feature_0_1',
                    'IN AFTER $spock_feature_0_1',
                    'OUT AFTER $spock_feature_0_1',
                    'OUT $spock_feature_0_1',

                    'IN $spock_feature_0_2',
                    'IN BEFORE $spock_feature_0_2',
                    'OUT BEFORE $spock_feature_0_2',
                    //  This test is $spock_feature_0_2
            ]
        then:
            assert calls.size() == expected.size()
            for (int i = 0; i < expected.size(); i++) {
                def a = calls.get(i)
                def b = expected[i]
                assert a == b
            }
    }

}
