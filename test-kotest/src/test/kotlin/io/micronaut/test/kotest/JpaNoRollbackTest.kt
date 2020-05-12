/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.test.kotest

import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micronaut.test.annotation.MicronautTest
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionDefinition
import javax.persistence.EntityManager

@MicronautTest(rollback = false)
@DbProperties
class JpaNoRollbackTest(
    private val entityManager: EntityManager,
    private val transactionManager: PlatformTransactionManager
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

    override fun afterSpec(spec: Spec) {
        val tx = transactionManager.getTransaction(DefaultTransactionDefinition())
        val criteriaBuilder = entityManager.criteriaBuilder
        val delete = criteriaBuilder.createCriteriaDelete(Book::class.java)
        delete.from(Book::class.java)
        entityManager.createQuery(delete).executeUpdate()
        transactionManager.commit(tx)
    }
}
