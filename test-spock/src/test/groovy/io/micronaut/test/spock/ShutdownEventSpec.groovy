package io.micronaut.test.spock

import io.micronaut.context.event.ShutdownEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(rebuildContext = true)
@Stepwise
class ShutdownEventSpec extends Specification {

    @Shared ShutdownEventListener sharedListener
    @Inject ShutdownEventListener listener

    void "test and shutdown"() {
        given:
        sharedListener = listener
        expect:
        listener.count == 0
    }

    void "check event count"() {
        expect:
        listener.count == 0
        sharedListener.count == 1
    }

    @Singleton
    static class ShutdownEventListener {
        int count = 0
        @EventListener
        void onShutdown(ShutdownEvent e) {
            count++
        }
    }
}
