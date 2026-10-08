package io.micronaut.test.junit5.rebuild;

import io.micronaut.aop.Around;
import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.junit5.DbProperties;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Micronaut around advice on a test method must still apply after the context is rebuilt, through
 * the interceptors of the rebuilt context.
 */
@MicronautTest(rebuildContext = true, transactional = false)
@Property(name = "spec.name", value = "RebuildContextInterceptedTest")
@Property(name = "datasources.default.url", value = "jdbc:h2:mem:RebuildContextInterceptedTest;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
@DbProperties
class RebuildContextInterceptedTest {

    @Test
    @Recorded
    void firstTestIsIntercepted() {
        assertInterceptedByRunningContext("firstTestIsIntercepted");
    }

    @Test
    @Recorded
    void secondTestIsIntercepted() {
        assertInterceptedByRunningContext("secondTestIsIntercepted");
    }

    private static void assertInterceptedByRunningContext(String methodName) {
        assertFalse(RecordingInterceptor.CALLS.isEmpty(), "the test method was intercepted");
        assertEquals(methodName + " running", RecordingInterceptor.CALLS.get(RecordingInterceptor.CALLS.size() - 1));
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Around
    @interface Recorded {
    }

    @Singleton
    @InterceptorBean(Recorded.class)
    @Requires(property = "spec.name", value = "RebuildContextInterceptedTest")
    static class RecordingInterceptor implements MethodInterceptor<Object, Object> {
        // the test instance of a proxied test class is not injected, so the calls are kept statically
        static final List<String> CALLS = new CopyOnWriteArrayList<>();

        private final ApplicationContext applicationContext;

        RecordingInterceptor(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        @Override
        public Object intercept(MethodInvocationContext<Object, Object> context) {
            CALLS.add(context.getMethodName() + (applicationContext.isRunning() ? " running" : " stopped"));
            return context.proceed();
        }
    }
}
