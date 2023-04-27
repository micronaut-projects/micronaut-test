package io.micronaut.test.spock.props

import io.micronaut.context.annotation.Value
import io.micronaut.test.extensions.spock.annotation.MicronautTest

@MicronautTest
class TestPropertiesProviderFactoryTest {

    @Value('${this-test-class}')
    String thisTestClass

    void "test properties provider is called"() {
        expect:
        thisTestClass == TestPropertiesProviderFactoryTest.class.name
    }

}
