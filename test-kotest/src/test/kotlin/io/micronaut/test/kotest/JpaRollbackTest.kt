
package io.micronaut.test.kotest

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micronaut.test.extensions.kotest.annotation.MicronautTest

import javax.persistence.EntityManager

@MicronautTest
@DbProperties
class JpaRollbackTest(private val entityManager: EntityManager): BehaviorSpec({

    given("rollback between tests") {
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
                entityManager.createQuery(query).resultList.size shouldBe 1
            }
        }
    }
})
