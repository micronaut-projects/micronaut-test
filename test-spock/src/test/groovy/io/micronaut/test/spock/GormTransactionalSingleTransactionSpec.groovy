
package io.micronaut.test.spock

import io.micronaut.context.ApplicationContext
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.annotation.TransactionMode
import io.micronaut.test.spock.entities.Book
import spock.lang.Specification
import spock.lang.Stepwise

import jakarta.inject.Inject

@MicronautTest(packages = "io.micronaut.test.spock.entities", transactionMode = TransactionMode.SINGLE_TRANSACTION)
@HibernateProperties
@Stepwise
class GormTransactionalSingleTransactionSpec extends Specification {

    @Inject
    ApplicationContext applicationContext

    def setup() {
        new Book(name: "The Shining").save(failOnError: true, flush: true)
    }

    def cleanup() {
        // check book from setup was rolled back
        assert Book.count() == 0
    }

    void "book was saved"() {
        expect:
        Book.count() == 1
    }

    void "book was saved again"() {
        expect:
        Book.count() == 1
    }
}
