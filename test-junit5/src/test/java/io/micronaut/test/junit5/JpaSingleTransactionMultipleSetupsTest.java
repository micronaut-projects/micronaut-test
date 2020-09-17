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
package io.micronaut.test.junit5;

import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.TransactionMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;

import static org.junit.Assert.assertEquals;

@MicronautTest(transactionMode = TransactionMode.SINGLE_TRANSACTION)
@DbProperties
public class JpaSingleTransactionMultipleSetupsTest {

    @Inject
    EntityManager entityManager;

    @BeforeEach
    void setUpOne() {
        final Book book = new Book();
        book.setTitle("The Stand");
        entityManager.persist(book);
    }

    @BeforeEach
    void setUpTwo() {
        final Book book = new Book();
        book.setTitle("The Shining");
        entityManager.persist(book);
    }

    @AfterEach
    void tearDown() {
        // check setups were rolled back
        final CriteriaQuery<Book> query = entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        assertEquals(0, entityManager.createQuery(query).getResultList().size());
    }

    @Test
    void testPersistOne() {
        final CriteriaQuery<Book> query = entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        assertEquals(2, entityManager.createQuery(query).getResultList().size());
    }

    @Test
    void testPersistTwo() {
        final CriteriaQuery<Book> query = entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        assertEquals(2, entityManager.createQuery(query).getResultList().size());
    }
}