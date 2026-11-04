package io.micronaut.test.spock.mockbean

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import spock.lang.Specification

/**
 * An interface whose method declares its own bounded type variables can be mocked from a Spock
 * spec, where the Groovy compiler generates the proxy's code.
 */
@MicronautTest
@Property(name = "spec.name", value = "GenericMethodMockBeanSpec")
class GenericMethodMockBeanSpec extends Specification {

    @Inject
    FooService fooService

    @Inject
    BarService barService

    void "a generic method mock is created and used"() {
        when:
        def result = fooService.alwaysTrue()

        then:
        1 * barService.send(_)
        result
    }

    @MockBean(BarService)
    BarService barService() {
        Mock(BarService)
    }
}

interface Command<R> {
    R execute()
}

interface BarService {
    public <R, C extends Command<R>> R send(C command)
}

@Singleton
@Requires(property = "spec.name", value = "GenericMethodMockBeanSpec")
class FooService {
    private final BarService barService

    FooService(BarService barService) {
        this.barService = barService
    }

    boolean alwaysTrue() {
        barService.send(null)
        true
    }
}
