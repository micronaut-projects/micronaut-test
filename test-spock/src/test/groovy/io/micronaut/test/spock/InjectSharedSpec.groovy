
package io.micronaut.test.spock

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import javax.inject.Inject

@MicronautTest
@Stepwise
class InjectSharedSpec extends Specification {

    @Shared
    @Inject
    MathService mathService

    def setupSpec() {
        assert mathService != null
    }

    void "test use shared 1"() {
        expect:
        mathService.compute(10) == 40
    }

    void "test use shared 2"() {
        expect:
        mathService.compute(10) == 40
    }
}
