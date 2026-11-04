package io.micronaut.test.spock.rebuild

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.context.TestMethodInterceptor
import io.micronaut.test.context.TestMethodInvocationContext
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import spock.lang.Specification

/**
 * With the annotation on a spec and on its parent, Spock adds the extension's interceptors once for
 * each, and a feature of the parent must still be wrapped by the context it uses.
 */
@MicronautTest(rebuildContext = true)
abstract class RebuildContextInterceptorsHierarchyParent extends Specification {

    @Inject
    ApplicationContext applicationContext

    boolean ranOnlyInsideTheInterceptorsOfItsOwnContext() {
        assert HierarchyRecordingInterceptor.INTERCEPTED
        assert HierarchyRecordingInterceptor.INTERCEPTED.every { it.is(applicationContext) }
        HierarchyRecordingInterceptor.INTERCEPTED.clear()
        true
    }

    void "a feature of the parent runs inside the interceptors of its own context"() {
        expect:
        ranOnlyInsideTheInterceptorsOfItsOwnContext()
    }
}

@MicronautTest(rebuildContext = true)
@Property(name = "spec.name", value = "RebuildContextInterceptorsHierarchySpec")
class RebuildContextInterceptorsHierarchySpec extends RebuildContextInterceptorsHierarchyParent {

    void "a feature of the spec runs inside the interceptors of its own context"() {
        expect:
        ranOnlyInsideTheInterceptorsOfItsOwnContext()
    }
}

@Singleton
@Requires(property = "spec.name", value = "RebuildContextInterceptorsHierarchySpec")
class HierarchyRecordingInterceptor implements TestMethodInterceptor<Object> {
    private final ApplicationContext applicationContext
    static final List<ApplicationContext> INTERCEPTED = []

    HierarchyRecordingInterceptor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext
    }

    @Override
    Object interceptTest(TestMethodInvocationContext<Object> methodInvocationContext) throws Throwable {
        INTERCEPTED << applicationContext
        return methodInvocationContext.proceed()
    }
}
