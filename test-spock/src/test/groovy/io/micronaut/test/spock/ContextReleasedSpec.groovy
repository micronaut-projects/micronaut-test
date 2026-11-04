package io.micronaut.test.spock

import io.micronaut.context.ApplicationContext
import io.micronaut.test.annotation.MicronautTestValue
import io.micronaut.test.annotation.TransactionMode
import io.micronaut.test.extensions.spock.MicronautSpockExtension
import spock.lang.Specification

import java.lang.ref.WeakReference

/**
 * A spec's bean definition is loaded for the rest of the run, and through it the extension stays
 * reachable, so the extension must let go of its application context once the spec has finished.
 */
class ContextReleasedSpec extends Specification {

    void "the application context is unreachable once the spec has finished"() {
        given:
        def extension = new MicronautSpockExtension()
        def value = new MicronautTestValue(void, [] as String[], [] as String[], [] as String[], false, false, false,
                [] as Class[], TransactionMode.SEPARATE_TRANSACTIONS, false, false, false)

        when:
        extension.beforeClass(null, ContextReleasedSpec, value)
        def context = new WeakReference<ApplicationContext>(extension.applicationContext)
        extension.afterClass(null)
        for (int i = 0; i < 100 && context.get() != null; i++) {
            System.gc()
            Thread.sleep(100)
        }

        then:
        context.get() == null
    }
}
