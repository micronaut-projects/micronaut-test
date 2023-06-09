package io.micronaut.test.kotest5.intercept

import io.micronaut.context.annotation.Property
import io.micronaut.test.context.TestContext
import io.micronaut.test.context.TestExecutionListener
import io.micronaut.test.context.TestMethodInterceptor
import io.micronaut.test.context.TestMethodInvocationContext
import jakarta.inject.Singleton

@Singleton
@Property(name = "InterceptTestSpec", value = "true")
class TestInterceptor : TestMethodInterceptor<Any>, TestExecutionListener {
    var calls: MutableList<String> = ArrayList()

    @Throws(Exception::class)
    override fun beforeTestClass(testContext: TestContext) {
        calls.add("BEFORE TEST CLASS " + testContext.testName)
        if (testContext.isSupportsTestMethodInterceptors) {
            throw IllegalStateException("Not supported")
        }
    }

    @Throws(Exception::class)
    override fun beforeSetupTest(testContext: TestContext) {
        calls.add("BEFORE SETUP TEST " + testContext.testName)
    }

    @Throws(Exception::class)
    override fun afterSetupTest(testContext: TestContext) {
        calls.add("AFTER SETUP TEST " + testContext.testName)
    }

    @Throws(Exception::class)
    override fun beforeCleanupTest(testContext: TestContext) {
        calls.add("BEFORE CLEANUP TEST " + testContext.testName)
    }

    @Throws(Exception::class)
    override fun afterCleanupTest(testContext: TestContext) {
        calls.add("AFTER CLEANUP TEST " + testContext.testName)
    }

    @Throws(Exception::class)
    override fun beforeTestMethod(testContext: TestContext) {
        calls.add("BEFORE TEST METHOD " + testContext.testName)
        if (testContext.isSupportsTestMethodInterceptors) {
            throw IllegalStateException("Not supported")
        }
    }

    @Throws(Exception::class)
    override fun beforeTestExecution(testContext: TestContext) {
        calls.add("BEFORE TEST EXECUTION " + testContext.testName)
    }

    @Throws(Exception::class)
    override fun afterTestExecution(testContext: TestContext) {
        calls.add("AFTER TEST EXECUTION " + testContext.testName)
    }

    @Throws(Exception::class)
    override fun afterTestMethod(testContext: TestContext) {
        calls.add("AFTER TEST METHOD " + testContext.testName)
    }

    @Throws(Exception::class)
    override fun afterTestClass(testContext: TestContext) {
        calls.add("AFTER TEST CLASS " + testContext.testName)
    }

    @Throws(Throwable::class)
    override fun interceptBeforeEach(methodInvocationContext: TestMethodInvocationContext<Any>): Any {
        calls.add("IN BEFORE " + methodInvocationContext.testContext.testName)
        return try {
            methodInvocationContext.proceed()
        } finally {
            calls.add("OUT BEFORE " + methodInvocationContext.testContext.testName)
        }
    }

    @Throws(Throwable::class)
    override fun interceptTest(methodInvocationContext: TestMethodInvocationContext<Any>): Any {
        calls.add("IN " + methodInvocationContext.testContext.testName)
        return try {
            methodInvocationContext.proceed()
        } finally {
            calls.add("OUT " + methodInvocationContext.testContext.testName)
        }
    }

    @Throws(Throwable::class)
    override fun interceptAfterEach(methodInvocationContext: TestMethodInvocationContext<Any>): Any {
        calls.add("IN AFTER " + methodInvocationContext.testContext.testName)
        return try {
            methodInvocationContext.proceed()
        } finally {
            calls.add("OUT AFTER " + methodInvocationContext.testContext.testName)
        }
    }
}
