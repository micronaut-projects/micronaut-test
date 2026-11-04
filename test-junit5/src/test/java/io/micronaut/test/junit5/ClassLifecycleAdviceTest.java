package io.micronaut.test.junit5;

import io.micronaut.aop.Around;
import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Around advice applies to a non-static {@code @BeforeAll} of the test class, which makes the class
 * a proxy. A PER_CLASS {@code @Nested} class still works then, but its own methods are not advised.
 */
@MicronautTest(transactional = false)
@Property(name = "datasources.default.url", value = "jdbc:h2:mem:ClassLifecycleAdviceTest;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
@DbProperties
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClassLifecycleAdviceTest {

    @Inject
    MathService mathService;

    @BeforeAll
    @Recorded
    void adviseMe() {
        assertNotNull(mathService);
    }

    @Test
    void adviceGetsTheTestInstanceAsItsTarget() {
        assertEquals(ClassLifecycleAdviceTest.class, RecordingInterceptor.TARGETS.get("adviseMe"));
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class PerClassNested {

        @Inject
        MathService ownMathService;

        Integer computedInBeforeAll;

        // same name as the advised method of the enclosing class, whose advice must not apply here
        @BeforeAll
        void adviseMe() {
            computedInBeforeAll = mathService.compute(2);
        }

        @Test
        void isInjectedWithAProxiedEnclosingClass() {
            assertNotNull(ownMathService);
            assertEquals(8, computedInBeforeAll);
        }

        @Test
        void itsClassLevelMethodsAreNotAdvised() {
            assertEquals(ClassLifecycleAdviceTest.class, RecordingInterceptor.TARGETS.get("adviseMe"));
        }
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Around
    @interface Recorded {
    }

    @Singleton
    @InterceptorBean(Recorded.class)
    static class RecordingInterceptor implements MethodInterceptor<Object, Object> {
        static final Map<String, Class<?>> TARGETS = new ConcurrentHashMap<>();

        @Override
        public Object intercept(MethodInvocationContext<Object, Object> context) {
            TARGETS.put(context.getMethodName(), context.getTarget().getClass());
            return context.proceed();
        }
    }
}
