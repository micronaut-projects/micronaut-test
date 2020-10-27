
package io.micronaut.test.spock

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import spock.lang.Specification

@MicronautTest
class PropertySourceMapSpec extends Specification implements TestPropertyProvider {
    @Property(name = "foo.bar")
    String val

    void "test inject properties"() {
        expect:
        val == 'one'
    }

    @Override
    Map<String, String> getProperties() {
        return ['foo.bar':'one', 'foo.baz':'two']
    }
}
