
package io.micronaut.test.kotlintest

import io.kotlintest.*
import io.kotlintest.extensions.TopLevelTest
import io.kotlintest.specs.BehaviorSpec
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionDefinition
import javax.persistence.EntityManager

@MicronautTest(rollback = false)
@DbProperties
class JpaNoRollbackTest(private val entityManager: EntityManager,
                        private val transactionManager: PlatformTransactionManager): BehaviorSpec({

    given("no rollback between tests") {
        `when`("test persist one") {
            val book = Book()
            book.title = "The Stand"
            entityManager.persist(book)

            then("the book is persisted") {
                entityManager.find(Book::class.java, book.id) shouldNotBe null

                val query = entityManager.criteriaBuilder.createQuery(Book::class.java)
                query.from(Book::class.java)
                entityManager.createQuery(query).resultList.size shouldBe 1
            }
        }
    }

    given("a new transaction") {
        `when`("test persist two") {
            val book = Book()
            book.title = "The Shining"
            entityManager.persist(book)

            then("the book is persisted") {
                entityManager.find(Book::class.java, book.id) shouldNotBe null

                val query = entityManager.criteriaBuilder.createQuery(Book::class.java)
                query.from(Book::class.java)
                entityManager.createQuery(query).resultList.size shouldBe 2
            }
        }
    }
}) {

    override fun afterSpecClass(spec: Spec, results: Map<TestCase, TestResult>) {
        val tx = transactionManager.getTransaction(DefaultTransactionDefinition())
        val criteriaBuilder = entityManager.criteriaBuilder
        val delete = criteriaBuilder.createCriteriaDelete(Book::class.java)
        delete.from(Book::class.java)
        entityManager.createQuery(delete).executeUpdate()
        transactionManager.commit(tx)
    }
}