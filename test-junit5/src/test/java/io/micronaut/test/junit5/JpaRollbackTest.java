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
package io.micronaut.test.junit5;

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@MicronautTest
@DbProperties
public class JpaRollbackTest {

    @Inject
    EntityManager entityManager;

    @Test
    void testPersistOne() {
        final Book book = new Book();
        book.setTitle("The Stand");
        entityManager.persist(book);
        assertNotNull(entityManager.find(Book.class, book.getId()));

        final CriteriaQuery<Book> query = entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        assertEquals(1, entityManager.createQuery(query).getResultList().size());
    }

    @Test
    void testPersistTwo() {
        final Book book = new Book();
        book.setTitle("The Shining");
        entityManager.persist(book);
        assertNotNull(entityManager.find(Book.class, book.getId()));

        final CriteriaQuery<Book> query =
                entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        assertEquals(1, entityManager.createQuery(query).getResultList().size());

    }
}
