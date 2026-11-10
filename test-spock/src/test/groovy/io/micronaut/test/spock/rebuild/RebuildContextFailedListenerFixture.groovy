package io.micronaut.test.spock.rebuild

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.context.TestContext
import io.micronaut.test.context.TestExecutionListener
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import spock.lang.IgnoreIf
import spock.lang.Specification

/**
 * A listener fails the first test before setup, so Spock never runs its cleanup. Its method
 * properties must still be restored, or the second test saves them as the class values and the
 * third test sees them. Only runs through {@link RebuildContextFailedStartSpec}.
 */
@IgnoreIf({ !System.getProperty(RebuildContextFailedListenerFixture.ENABLED) })
@MicronautTest(rebuildContext = true)
@Property(name = "spec.name", value = "RebuildContextFailedListenerFixture")
@Property(name = "rebuild.value", value = "class")
class RebuildContextFailedListenerFixture extends Specification {

    static final String ENABLED = "rebuild.failed-listener-fixture"
    static final String FAIL_LISTENER = "rebuild.fail-listener"

    @Inject
    ApplicationContext applicationContext

    @Property(name = "rebuild.value", value = "first")
    @Property(name = FAIL_LISTENER, value = "true")
    void "a listener fails before setup"() {
        expect:
        true
    }

    @Property(name = "rebuild.value", value = "second")
    void "the next test overrides the property"() {
        expect:
        applicationContext.getProperty("rebuild.value", String).get() == "second"
        !applicationContext.containsProperty(FAIL_LISTENER)
    }

    void "a test without overrides sees the class value"() {
        expect:
        applicationContext.getProperty("rebuild.value", String).get() == "class"
    }

    @Singleton
    @Requires(property = "spec.name", value = "RebuildContextFailedListenerFixture")
    @Requires(property = FAIL_LISTENER, value = "true")
    static class FailingListener implements TestExecutionListener {
        @Override
        void beforeTestMethod(TestContext testContext) {
            throw new IllegalStateException("listener failure")
        }
    }
}
