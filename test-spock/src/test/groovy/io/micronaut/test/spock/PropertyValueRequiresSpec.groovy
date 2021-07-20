
package io.micronaut.test.spock

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Issue
import spock.lang.Specification

import jakarta.inject.Inject
import jakarta.inject.Singleton

@Issue("https://github.com/micronaut-projects/micronaut-test/issues/91")
@MicronautTest(rebuildContext = true)
@Property(name = "foo.bar", value = "stuff")
class PropertyValueRequiresSpec extends Specification {
    @Inject
    MyService myService

    void "test initial value"() {
        expect:
        myService instanceof MyServiceStuff
    }

    @Property(name = "foo.bar", value = "changed")
    void "test value changed"() {
        expect:
        myService instanceof MyServiceChanged
    }

    void "test value restored"() {
        expect:
        myService instanceof MyServiceStuff
    }
}

interface MyService {}

@Singleton
@Requires(property = "foo.bar", value = "stuff")
class MyServiceStuff implements MyService {}


@Singleton
@Requires(property = "foo.bar", value = "changed")
class MyServiceChanged implements MyService {}
