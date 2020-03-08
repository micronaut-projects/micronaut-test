package io.micronaut.test.kotest

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldNotBe
import io.micronaut.test.annotation.MicronautTest
import org.springframework.transaction.PlatformTransactionManager
import javax.persistence.EntityManager

@MicronautTest
@DbProperties
class JpaRollbackStringSpecTest(private val entityManager: EntityManager,
                                private val transactionManager: PlatformTransactionManager) : StringSpec() {
    init {
        "given: rollback between tests, when: test persist one, then: the book is persisted" {
            val book = Book()
            book.title = "The Stand"
            entityManager.persist(book)

            entityManager.find(Book::class.java, book.id) shouldNotBe null

            val query = entityManager.criteriaBuilder.createQuery(Book::class.java)
            query.from(Book::class.java)
            entityManager.createQuery(query).resultList shouldHaveSize 1
        }

        "given: a new transaction, when: test persist two, then: the book is persisted" {
            val book = Book()
            book.title = "The Shining"
            entityManager.persist(book)

            entityManager.find(Book::class.java, book.id) shouldNotBe null

            val query = entityManager.criteriaBuilder.createQuery(Book::class.java)
            query.from(Book::class.java)
            entityManager.createQuery(query).resultList shouldHaveSize 1

        }
    }
}
