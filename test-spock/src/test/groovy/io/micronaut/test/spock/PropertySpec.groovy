
package io.micronaut.test.spock

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Value
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import spock.lang.Stepwise

@MicronautTest
@Property(name = "foo.bar", value = "stuff")
@Stepwise
class PropertySpec extends Specification {


    @Value('${foo.bar}')
    String val

    @Value('${prop.from.yml}')
    String fromYml

    void "test value"() {
        expect:
        val == 'stuff'
    }

    @Property(name = "foo.bar", value = "changed")
    void "test value changed"() {
        expect:
        val == 'changed'
    }

    void "test value restored"() {
        expect:
        val == 'stuff'
    }

    @Property(name = "prop.from.yml", value = "local")
    void "test overridden from config"() {
        expect:
        fromYml == "local"
    }

    void "test value from config"() {
        expect:
        fromYml == "yml"
    }

}
