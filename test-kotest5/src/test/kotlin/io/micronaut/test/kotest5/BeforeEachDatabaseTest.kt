package io.micronaut.test.kotest5

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import jakarta.persistence.EntityManager

/**
 * Database work in a Kotest beforeEach hook runs inside the test transaction.
 */
@MicronautTest
@DbProperties
class BeforeEachDatabaseTest(private val entityManager: EntityManager) : StringSpec() {

    override suspend fun beforeEach(testCase: TestCase) {
        val book = Book()
        book.title = "Inserted in beforeEach"
        entityManager.persist(book)
    }

    init {
        "beforeEach data is visible" {
            val query = entityManager.criteriaBuilder.createQuery(Book::class.java)
            query.from(Book::class.java)
            entityManager.createQuery(query).resultList.size shouldBe 1
        }

        "beforeEach data is rolled back with each test" {
            val query = entityManager.criteriaBuilder.createQuery(Book::class.java)
            query.from(Book::class.java)
            entityManager.createQuery(query).resultList.size shouldBe 1
        }
    }
}
