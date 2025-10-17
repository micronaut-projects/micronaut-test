/*
 * Copyright 2017-2025 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.test.leak;

import io.netty.util.LeakPresenceDetector;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Junit 5 extension for leak detection using {@link LeakPresenceDetector}.
 * <p>
 * Leak presence is checked at the class level. Any resource must be closed at the end of the test class, by the time
 * {@link org.junit.jupiter.api.AfterAll} has been called. Method-level detection is not possible because some tests
 * retain resources between methods on the same class, notably parameterized tests that allocate different buffers
 * before running the test methods with those buffers.
 * <p>
 * This extension supports parallel test execution, but has to make some assumptions about the thread lifecycle. The
 * resource scope for the class is created in {@link org.junit.jupiter.api.BeforeAll}, and then saved in a thread local
 * on each {@link org.junit.jupiter.api.BeforeEach}. This appears to work well with junit's default parallelism,
 * despite the use of a fork-join pool that can transfer tasks between threads, but it may lead to problems if tests
 * make use of fork-join machinery themselves.
 * <p>
 * The ThreadLocal holding the scope is {@link InheritableThreadLocal inheritable}, so that e.g. event loops created
 * in a test are assigned to the test resource scope.
 */
public final class JupiterLeakPresenceExtension
    implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

    // NOTE: This class is also present in an internal netty module: https://github.com/netty/netty/blob/4.2/testsuite-common/src/main/java/io/netty/util/test/LeakPresenceExtension.java

    private static final Object SCOPE_KEY = new Object();
    private static final Object PREVIOUS_SCOPE_KEY = new Object();

    static {
        WithTransferableScope.init();
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.GLOBAL);
        if (store.get(SCOPE_KEY) != null) {
            throw new IllegalStateException("Weird context lifecycle");
        }
        ScopeWrapper scope = new ScopeWrapper(new LeakPresenceDetector.ResourceScope(context.getDisplayName()));
        store.put(SCOPE_KEY, scope);

        WithTransferableScope.SCOPE.set(scope.scope);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        ScopeWrapper outerScope;
        ExtensionContext outerContext = context;
        while (true) {
            outerScope = (ScopeWrapper)
                outerContext.getStore(ExtensionContext.Namespace.GLOBAL).get(SCOPE_KEY);
            if (outerScope != null) {
                break;
            }
            outerContext = outerContext.getParent()
                .orElseThrow(() -> new IllegalStateException("No resource scope found"));
        }

        LeakPresenceDetector.ResourceScope previousScope = WithTransferableScope.SCOPE.get();
        WithTransferableScope.SCOPE.set(outerScope.scope);
        if (previousScope != null) {
            context.getStore(ExtensionContext.Namespace.GLOBAL).put(PREVIOUS_SCOPE_KEY, new ScopeWrapper(previousScope));
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        ScopeWrapper previousScope = (ScopeWrapper)
            context.getStore(ExtensionContext.Namespace.GLOBAL).get(PREVIOUS_SCOPE_KEY);
        if (previousScope != null) {
            WithTransferableScope.SCOPE.set(previousScope.scope);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws InterruptedException {
        ScopeWrapper scope =
            (ScopeWrapper) context.getStore(ExtensionContext.Namespace.GLOBAL).get(SCOPE_KEY);

        WithTransferableScope.closeWithGracePeriod(scope.scope);
    }

    /**
     * Prevent junit from closing the ResourceScope automatically.
     */
    private record ScopeWrapper(LeakPresenceDetector.ResourceScope scope) {
    }
}
