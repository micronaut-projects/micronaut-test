package io.micronaut.test.kotest5

import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.micronaut.transaction.SynchronousTransactionManager
import io.micronaut.transaction.support.DefaultTransactionDefinition
import jakarta.persistence.EntityManager

@MicronautTest(rollback = false)
@DbProperties
class JpaNoRollbackTest(
    private val entityManager: EntityManager,
    private val transactionManager: SynchronousTransactionManager<Any>
) : BehaviorSpec() {

    init {
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
    }

    override suspend fun afterSpec(spec: Spec) {
        val tx = transactionManager.getTransaction(DefaultTransactionDefinition())
        val criteriaBuilder = entityManager.criteriaBuilder
        val delete = criteriaBuilder.createCriteriaDelete(Book::class.java)
        delete.from(Book::class.java)
        entityManager.createQuery(delete).executeUpdate()
        transactionManager.commit(tx)
    }
}
