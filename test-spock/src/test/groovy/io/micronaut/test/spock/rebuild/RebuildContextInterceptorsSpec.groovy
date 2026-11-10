package io.micronaut.test.spock.rebuild

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.context.TestContext
import io.micronaut.test.context.TestExecutionListener
import io.micronaut.test.context.TestMethodInterceptor
import io.micronaut.test.context.TestMethodInvocationContext
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification

/**
 * Each test must run inside the interceptors of the context it uses, and get its listeners from it,
 * although Spock enters the feature's interceptor before the setup interceptor.
 */
@MicronautTest(rebuildContext = true)
@Property(name = "spec.name", value = "RebuildContextInterceptorsSpec")
class RebuildContextInterceptorsSpec extends Specification {

    @Inject
    ApplicationContext applicationContext

    @Inject
    RecordingInterceptor interceptor

    @Inject
    RecordingListener listener

    @Shared
    List<ApplicationContext> seenContexts = []

    @Shared
    List<ApplicationContext> retriedContexts = []

    void "the first test runs inside the interceptors of its own context"() {
        expect:
        ranInsideItsOwnContext()
    }

    void "the second test runs inside the interceptors of its own context"() {
        expect:
        ranInsideItsOwnContext()
    }

    void "each iteration runs inside the interceptors of its own context"() {
        expect:
        ranInsideItsOwnContext()

        where:
        iteration << [1, 2]
    }

    @Property(name = "rebuild.method-property", value = "set")
    void "a test with method properties runs inside the interceptors of its own context"() {
        expect:
        applicationContext.getProperty("rebuild.method-property", String).get() == "set"
        ranInsideItsOwnContext()
    }

    @Retry(count = 1, mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
    void "a test retried with its setup gets a new context"() {
        given:
        retriedContexts << applicationContext

        expect:
        applicationContext.running
        retriedContexts.size() == 2
        !retriedContexts[0].is(retriedContexts[1])
    }

    private boolean ranInsideItsOwnContext() {
        assert applicationContext.running
        assert interceptor.interceptedContext.is(applicationContext)
        assert interceptor.testContextApplicationContext.is(applicationContext)
        assert listener.beforeTestMethodContext.is(applicationContext)
        assert !seenContexts.any { it.is(applicationContext) }
        seenContexts << applicationContext
        true
    }

    @Singleton
    @Requires(property = "spec.name", value = "RebuildContextInterceptorsSpec")
    static class RecordingInterceptor implements TestMethodInterceptor<Object> {
        private final ApplicationContext applicationContext
        volatile ApplicationContext interceptedContext
        volatile ApplicationContext testContextApplicationContext

        RecordingInterceptor(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext
        }

        @Override
        Object interceptTest(TestMethodInvocationContext<Object> methodInvocationContext) throws Throwable {
            interceptedContext = applicationContext
            testContextApplicationContext = methodInvocationContext.testContext.applicationContext
            return methodInvocationContext.proceed()
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = "RebuildContextInterceptorsSpec")
    static class RecordingListener implements TestExecutionListener {
        volatile ApplicationContext beforeTestMethodContext

        @Override
        void beforeTestMethod(TestContext testContext) {
            beforeTestMethodContext = testContext.applicationContext
        }
    }
}
