
package io.micronaut.test.spock

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.extensions.spock.MicronautSpockExtension
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.SpecInfo
import spock.lang.Specification

import java.lang.reflect.Field
import java.util.ArrayDeque
import java.util.Queue

@MicronautTest
class FixtureWithAnnotationSpec extends Specification {
    static Boolean firstTestPassed = false
    static Boolean setupFlag = false
    static Boolean cleanFlag = false
    static Boolean specFlag = false

    void setup() {
        setupFlag = true
    }

    void setupSpec() {
        specFlag = true
    }

    void cleanup() {
        cleanFlag = true
    }

    void "Setup should work"() {
        when: "fixture has been executed"
        firstTestPassed = true
        then: "setupFlag should be true"
        setupFlag
    }

    void "SetupSpec should work"() {
        when: "setupSpec has been executed"
        firstTestPassed = true
        then: "setupSpec should be true"
        specFlag
    }

    void "Cleanup should work"() {
        when: "cleanup has been executed"
        then: "cleanFlag should be true if not first test"
        firstTestPassed && cleanFlag
        cleanup:
        firstTestPassed = true
    }

    void "Cleanup should work Bis in case the first one passed first"() {
        when: "cleanup has been executed"
        then: "cleanFlag should be true if not first test"
        firstTestPassed && cleanFlag
        cleanup:
        firstTestPassed = true
    }

    def "CleanupSpec should work"() {
        given:
        SpecInfo specInfo = new SpecInfo()
        IMethodInvocation invocation = Mock()

        and:
        MicronautSpockExtension micronautSpockExtension = new MicronautSpockExtension()

        when: "cleanup spec interceptor is registered"
        micronautSpockExtension.visitSpecAnnotation((MicronautTest) null, specInfo)

        and: "interceptor is executed"
        specInfo.cleanupSpecInterceptors[0].intercept(invocation)

        then:
        1 * invocation.proceed()
    }

    def "CleanupSpec should retain the cleanupSpec failure and suppress teardown failures"() {
        given:
        SpecInfo specInfo = new SpecInfo()
        IMethodInvocation invocation = Mock()
        RuntimeException cleanupFailure = new RuntimeException("cleanupSpec failed")
        Exception afterTestClassFailure = new Exception("afterTestClass failed")
        RuntimeException afterClassFailure = new RuntimeException("afterClass failed")
        RuntimeException singletonMocksFailure = new RuntimeException("singletonMocks.clear failed")
        TestMicronautSpockExtension micronautSpockExtension = new TestMicronautSpockExtension(
                afterTestClassFailure: afterTestClassFailure,
                afterClassFailure: afterClassFailure
        )
        setSingletonMocks(micronautSpockExtension, new ThrowingQueue(failure: singletonMocksFailure))

        when:
        micronautSpockExtension.visitSpecAnnotation((MicronautTest) null, specInfo)
        specInfo.cleanupSpecInterceptors[0].intercept(invocation)

        then:
        RuntimeException e = thrown()
        e.is(cleanupFailure)
        e.suppressed as List == [afterTestClassFailure, afterClassFailure, singletonMocksFailure]
        1 * invocation.proceed() >> { throw cleanupFailure }
    }

    def "CleanupSpec should throw the first teardown failure when cleanupSpec succeeds"() {
        given:
        SpecInfo specInfo = new SpecInfo()
        IMethodInvocation invocation = Mock()
        Exception afterTestClassFailure = new Exception("afterTestClass failed")
        RuntimeException afterClassFailure = new RuntimeException("afterClass failed")
        RuntimeException singletonMocksFailure = new RuntimeException("singletonMocks.clear failed")
        TestMicronautSpockExtension micronautSpockExtension = new TestMicronautSpockExtension(
                afterTestClassFailure: afterTestClassFailure,
                afterClassFailure: afterClassFailure
        )
        setSingletonMocks(micronautSpockExtension, new ThrowingQueue(failure: singletonMocksFailure))

        when:
        micronautSpockExtension.visitSpecAnnotation((MicronautTest) null, specInfo)
        specInfo.cleanupSpecInterceptors[0].intercept(invocation)

        then:
        Exception e = thrown()
        e.is(afterTestClassFailure)
        e.suppressed as List == [afterClassFailure, singletonMocksFailure]
        1 * invocation.proceed()
    }

    private static void setSingletonMocks(MicronautSpockExtension extension, Queue<Object> singletonMocks) {
        Field field = MicronautSpockExtension.getDeclaredField("singletonMocks")
        field.accessible = true
        field.set(extension, singletonMocks)
    }

    static class TestMicronautSpockExtension extends MicronautSpockExtension {
        Throwable afterTestClassFailure
        Throwable afterClassFailure

        @Override
        void afterTestClass(io.micronaut.test.context.TestContext testContext) throws Exception {
            throwFailure(afterTestClassFailure)
        }

        @Override
        protected void afterClass(IMethodInvocation context) {
            throwFailure(afterClassFailure)
        }

        private static void throwFailure(Throwable failure) throws Exception {
            if (failure == null) {
                return
            }
            if (failure instanceof Exception) {
                throw (Exception) failure
            }
            throw (Error) failure
        }
    }

    static class ThrowingQueue extends ArrayDeque<Object> {
        RuntimeException failure

        @Override
        void clear() {
            throw failure
        }
    }
}
