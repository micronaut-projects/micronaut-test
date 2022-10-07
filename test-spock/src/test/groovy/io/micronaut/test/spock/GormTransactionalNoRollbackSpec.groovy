
package io.micronaut.test.spock

import io.micronaut.context.ApplicationContext
import io.micronaut.spring.tx.test.SpringTransactionTestExecutionListener
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.spock.entities.Book
import org.springframework.transaction.support.TransactionSynchronizationManager
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Stepwise

import jakarta.inject.Inject

@MicronautTest(rollback = false, packages = "io.micronaut.test.spock.entities")
@HibernateProperties
@Stepwise
@Ignore("GORM is not supported yet")
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
