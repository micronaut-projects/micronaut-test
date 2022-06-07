package io.micronaut.test.spock.intercept

import io.micronaut.context.annotation.Property
import io.micronaut.test.context.TestMethodInterceptor
import io.micronaut.test.context.TestMethodInvocationContext
import jakarta.inject.Singleton

import java.lang.reflect.Method

@Singleton
@Property(name = "InterceptTestSpec", value = "true")
class TestInterceptor implements TestMethodInterceptor<Object> {

    public List<String> calls = new ArrayList<>()

    @Override
    Object interceptBeforeEach(TestMethodInvocationContext methodInvocationContext) throws Throwable {
        Method method = (Method) methodInvocationContext.getTestContext().getTestMethod()
        calls.add("IN BEFORE " + method.getName())
        try {
            return methodInvocationContext.proceed()
        } finally {
            calls.add("OUT BEFORE " + method.getName())
        }
    }

    @Override
    Object interceptTest(TestMethodInvocationContext methodInvocationContext) throws Throwable {
        Method method = (Method) methodInvocationContext.getTestContext().getTestMethod()
        calls.add("IN " + method.getName())
        try {
            return methodInvocationContext.proceed()
        } finally {
            calls.add("OUT " + method.getName())
        }
    }

    @Override
    Object interceptAfterEach(TestMethodInvocationContext methodInvocationContext) throws Throwable {
        Method method = (Method) methodInvocationContext.getTestContext().getTestMethod()
        calls.add("IN AFTER " + method.getName())
        try {
            return methodInvocationContext.proceed()
        } finally {
            calls.add("OUT AFTER " + method.getName())
        }
    }
}
