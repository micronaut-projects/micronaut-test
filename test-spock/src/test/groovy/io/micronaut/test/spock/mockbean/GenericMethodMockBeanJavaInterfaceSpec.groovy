package io.micronaut.test.spock.mockbean

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import spock.lang.PendingFeature
import spock.lang.Specification

/**
 * An interface compiled before the spec, whose method declares its own bounded type variables,
 * should be mockable from a Spock spec. See GenericMethodMockBeanSpec for what works today.
 */
@MicronautTest
@Property(name = "spec.name", value = "GenericMethodMockBeanJavaInterfaceSpec")
class GenericMethodMockBeanJavaInterfaceSpec extends Specification {

    @Inject
    Caller caller

    @Inject
    JavaCommandBus commandBus

    @PendingFeature(exceptions = NoSuchMethodError, reason = "https://github.com/micronaut-projects/micronaut-test/issues/1523: the Groovy visitor erases a bounded type variable of a compiled class to Object rather than its bound, so the proxy does not override the method, which needs a change in micronaut-core: https://github.com/micronaut-projects/micronaut-core/issues/14269")
    void "a generic method mock is created and used"() {
        when:
        def result = caller.alwaysTrue()

        then:
        1 * commandBus.send(_)
        result
    }

    @MockBean(JavaCommandBus)
    JavaCommandBus commandBus() {
        Mock(JavaCommandBus)
    }

    @Singleton
    @Requires(property = "spec.name", value = "GenericMethodMockBeanJavaInterfaceSpec")
    static class Caller {
        private final JavaCommandBus commandBus

        Caller(JavaCommandBus commandBus) {
            this.commandBus = commandBus
        }

        boolean alwaysTrue() {
            commandBus.send(null)
            true
        }
    }
}
