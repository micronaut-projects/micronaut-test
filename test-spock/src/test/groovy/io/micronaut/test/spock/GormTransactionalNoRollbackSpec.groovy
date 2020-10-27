
package io.micronaut.test.spock

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.core.util.StringUtils
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.spock.entities.Book
import io.micronaut.test.transaction.spring.SpringTransactionTestExecutionListener
import org.springframework.transaction.support.TransactionSynchronizationManager
import spock.lang.Specification
import spock.lang.Stepwise

import javax.inject.Inject

@MicronautTest(rollback = false, packages = "io.micronaut.test.spock.entities")
@HibernateProperties
@Stepwise
class GormTransactionalNoRollbackSpec extends Specification {

    @Inject
    ApplicationContext applicationContext

    def setup() {
        // check transaction is present in setup
        assert TransactionSynchronizationManager.isSynchronizationActive()
    }

    def cleanup() {
        // check transaction is present in cleanup
        assert TransactionSynchronizationManager.isSynchronizationActive()
    }

    void "bean SpringTransactionTestExecutionListener exists"() {
        expect:
        applicationContext.containsBean(SpringTransactionTestExecutionListener)
    }

    void "save book"() {
        when:
        new Book(name: "BAR").save(failOnError: true, flush: true)

        then:
        noExceptionThrown()

        and:
        Book.count() == old(Book.count()) + 1
    }

    void "book was not rolled back"() {
        expect:
        Book.count() == 1

        cleanup:
        Book.where {}.deleteAll()
    }
}
