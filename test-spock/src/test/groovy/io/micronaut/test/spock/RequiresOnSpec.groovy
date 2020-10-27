
package io.micronaut.test.spock

import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import spock.lang.Stepwise

@MicronautTest
@Requires(property = "does.not.exist")
@Stepwise
class RequiresOnSpec extends Specification {

    void "test requires disables test"() {
        expect:"This test should never run, as the test doesn't meet requirements"
        true == false
    }


}
