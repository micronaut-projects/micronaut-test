
package io.micronaut.test.junit5;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
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

        final CriteriaQuery<Book> query = entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        assertEquals(1, entityManager.createQuery(query).getResultList().size());
    }
}
