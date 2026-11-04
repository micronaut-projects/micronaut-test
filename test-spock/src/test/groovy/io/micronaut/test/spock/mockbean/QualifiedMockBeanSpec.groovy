package io.micronaut.test.spock.mockbean

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Qualifier
import jakarta.inject.Singleton
import spock.lang.Specification

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * A mock replacing a qualified bean declares the same qualifier, so qualified injection points find it.
 */
@MicronautTest
@Property(name = "spec.name", value = "QualifiedMockBeanSpec")
class QualifiedMockBeanSpec extends Specification {

    @Inject
    @Handler("plain")
    PlainHandler handler

    @Inject
    PlainConsumer consumer

    void "a qualified injection point gets the mock"() {
        when:
        def result = consumer.handle()

        then:
        1 * handler.handle() >> "mock"
        result == "mock"
    }

    // tag::mock[]
    @MockBean(DefaultPlainHandler)
    @Handler("plain") // <1>
    PlainHandler plainHandler() {
        Mock(PlainHandler)
    }
    // end::mock[]

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target([ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD])
    @interface Handler {
        String value()
    }

    interface PlainHandler {
        String handle()
    }

    @Singleton
    @Handler("plain")
    @Requires(property = "spec.name", value = "QualifiedMockBeanSpec")
    static class DefaultPlainHandler implements PlainHandler {
        @Override
        String handle() {
            "real"
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = "QualifiedMockBeanSpec")
    static class PlainConsumer {
        private final PlainHandler handler

        PlainConsumer(@Handler("plain") PlainHandler handler) {
            this.handler = handler
        }

        String handle() {
            handler.handle()
        }
    }
}
