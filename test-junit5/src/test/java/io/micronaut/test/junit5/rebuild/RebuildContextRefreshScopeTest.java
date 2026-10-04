package io.micronaut.test.junit5.rebuild;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.context.scope.Refreshable;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A {@code @Refreshable} bean created while the rebuilt context starts must be refreshed by the
 * refresh the extension sends before each test, which only happens when the extension sends it to
 * the new context's refresh scope rather than to the stopped context's.
 */
@MicronautTest(rebuildContext = true)
@Property(name = "spec.name", value = "RebuildContextRefreshScopeTest")
class RebuildContextRefreshScopeTest {

    @Inject
    ApplicationContext applicationContext;

    @Inject
    StartupRecorder startupRecorder;

    @Inject
    RefreshableBean refreshableBean;

    @Test
    void firstTestRefreshesTheRebuiltContext() {
        assertRefreshedInRebuiltContext();
    }

    @Test
    void secondTestRefreshesTheRebuiltContext() {
        assertRefreshedInRebuiltContext();
    }

    private void assertRefreshedInRebuiltContext() {
        assertTrue(applicationContext.isRunning());
        RefreshableBean atStartup = startupRecorder.atStartup;
        assertNotNull(atStartup, "the rebuilt context created the refreshable bean while starting");
        assertTrue(atStartup.disposed, "the refresh before the test reached the rebuilt context's refresh scope");
        RefreshableBean current = refreshableBean.self();
        assertNotSame(atStartup, current);
        assertFalse(current.disposed);
    }

    @Refreshable
    @Requires(property = "spec.name", value = "RebuildContextRefreshScopeTest")
    static class RefreshableBean {
        volatile boolean disposed;

        RefreshableBean self() {
            return this;
        }

        @PreDestroy
        void dispose() {
            disposed = true;
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = "RebuildContextRefreshScopeTest")
    static class StartupRecorder implements ApplicationEventListener<StartupEvent> {
        private final RefreshableBean refreshableBean;
        volatile RefreshableBean atStartup;

        StartupRecorder(RefreshableBean refreshableBean) {
            this.refreshableBean = refreshableBean;
        }

        @Override
        public void onApplicationEvent(StartupEvent event) {
            atStartup = refreshableBean.self();
        }
    }
}
