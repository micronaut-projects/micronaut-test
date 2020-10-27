
package io.micronaut.test.spock

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

@MicronautTest(propertySources = "myprops.properties")
class PropertySourceSpec extends Specification {

    @Property(name = "foo.bar")
    String val

    void "test property source"() {
        expect:
        val == 'foo'
    }
}
