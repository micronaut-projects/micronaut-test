
package io.micronaut.test.spock.base

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.spock.MathService
import jakarta.inject.Inject
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

@MicronautTest
@Stepwise
abstract class AbstractSharedSpec extends Specification {

    @Shared
    @Inject
    MathService mathService
}
