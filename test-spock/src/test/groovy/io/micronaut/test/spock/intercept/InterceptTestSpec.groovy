package io.micronaut.test.spock.intercept

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.spock.MathService
import jakarta.inject.Inject
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
@MicronautTest
@Property(name = "InterceptTestSpec", value = "true")
class InterceptTestSpec extends Specification {

    @Inject
    MathService mathService

    @Inject
    TestInterceptor testInterceptor

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
                    'OUT $spock_feature_0_0', // This is the implicit unroll
                    'IN $spock_feature_0_0',
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
            calls == expected
    }
}
