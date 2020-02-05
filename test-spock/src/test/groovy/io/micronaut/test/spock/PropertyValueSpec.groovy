package io.micronaut.test.spock

import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification
import spock.lang.Stepwise

@MicronautTest
@Property(name = "foo.bar", value = "stuff")
@Stepwise
class PropertyValueSpec extends Specification {

    @Property(name = "foo.bar")
    String val

    void "test initial value"() {
        expect:
        val == "stuff"
    }

    @Property(name = "foo.bar", value = "changed")
    void "test value changed"() {
        expect:
        val == "changed"
    }

    void "test value restored"() {
        expect:
        val == "stuff"
    }
}
