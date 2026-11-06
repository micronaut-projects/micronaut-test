package io.micronaut.test.spock.rebuild

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.context.scope.Refreshable
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.annotation.PreDestroy
import jakarta.inject.Inject
import jakarta.inject.Singleton
import spock.lang.Specification

/**
 * A {@code @Refreshable} bean created while the rebuilt context starts must be refreshed by the
 * refresh the extension sends before each test, which only happens when the extension sends it to
 * the new context's refresh scope rather than to the stopped context's.
 */
@MicronautTest(rebuildContext = true)
@Property(name = "spec.name", value = "RebuildContextRefreshScopeSpec")
class RebuildContextRefreshScopeSpec extends Specification {

    @Inject
    ApplicationContext applicationContext

    @Inject
    StartupRecorder startupRecorder

    @Inject
    RefreshableBean refreshableBean

    void "the first test refreshes the rebuilt context"() {
        expect:
        refreshedInRebuiltContext()
    }

    void "the second test refreshes the rebuilt context"() {
        expect:
        refreshedInRebuiltContext()
    }

    private boolean refreshedInRebuiltContext() {
        assert applicationContext.running
        RefreshableBean atStartup = startupRecorder.atStartup
        assert atStartup != null
        assert atStartup.disposed
        RefreshableBean current = refreshableBean.self()
        assert !current.is(atStartup)
        assert !current.disposed
        true
    }

    @Refreshable
    @Requires(property = "spec.name", value = "RebuildContextRefreshScopeSpec")
    static class RefreshableBean {
        volatile boolean disposed

        RefreshableBean self() {
            this
        }

        @PreDestroy
        void dispose() {
            disposed = true
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = "RebuildContextRefreshScopeSpec")
    static class StartupRecorder implements ApplicationEventListener<StartupEvent> {
        private final RefreshableBean refreshableBean
        volatile RefreshableBean atStartup

        StartupRecorder(RefreshableBean refreshableBean) {
            this.refreshableBean = refreshableBean
        }

        @Override
        void onApplicationEvent(StartupEvent event) {
            atStartup = refreshableBean.self()
        }
    }
}
