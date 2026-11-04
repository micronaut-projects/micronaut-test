package io.micronaut.test.spock.rebuild

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.IgnoreIf
import spock.lang.Specification

/**
 * The method properties of the first test stop its rebuilt context from starting, and must not be
 * carried over to the next test. Only runs through {@link RebuildContextFailedStartSpec}.
 */
@IgnoreIf({ !System.getProperty(RebuildContextFailedStartFixture.ENABLED) })
@MicronautTest(rebuildContext = true)
@Property(name = "spec.name", value = "RebuildContextFailedStartFixture")
class RebuildContextFailedStartFixture extends Specification {

    static final String ENABLED = "rebuild.failed-start-fixture"
    static final String FAIL_STARTUP = "rebuild.fail-startup"

    @Inject
    ApplicationContext applicationContext

    @Property(name = "rebuild.fail-startup", value = "true")
    void "the context fails to start"() {
        expect:
        true
    }

    void "the next test does not see the property"() {
        expect:
        !applicationContext.containsProperty(FAIL_STARTUP)
    }

    @Context
    @Requires(property = "spec.name", value = "RebuildContextFailedStartFixture")
    @Requires(property = "rebuild.fail-startup", value = "true")
    static class FailingBean {
        FailingBean() {
            throw new IllegalStateException("startup failure")
        }
    }
}
