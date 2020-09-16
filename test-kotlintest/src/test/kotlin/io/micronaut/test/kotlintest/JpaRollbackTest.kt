/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.test.kotlintest

import io.kotlintest.Spec
import io.kotlintest.TestCase
import io.kotlintest.extensions.TopLevelTest
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.BehaviorSpec
import io.micronaut.test.annotation.MicronautTest
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionDefinition

import javax.persistence.EntityManager

@MicronautTest
@DbProperties
class JpaRollbackTest(private val entityManager: EntityManager,
                      private val transactionManager: PlatformTransactionManager): BehaviorSpec({

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
