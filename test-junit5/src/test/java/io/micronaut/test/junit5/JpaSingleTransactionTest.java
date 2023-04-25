package io.micronaut.test.junit5;

import io.micronaut.test.annotation.TransactionMode;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(transactionMode = TransactionMode.SINGLE_TRANSACTION)
@DbProperties
public class JpaSingleTransactionTest {

    @Inject
    EntityManager entityManager;

    @BeforeEach
    void setUp() {
        final Book book = new Book();
        book.setTitle("The Stand");
        entityManager.persist(book);
    }

    @AfterEach
    void tearDown() {
        // check setup was rolled back
        final CriteriaQuery<Book> query = entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        assertEquals(0, entityManager.createQuery(query).getResultList().size());
    }

    @Test
    void testPersistOne() {
        final CriteriaQuery<Book> query = entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        assertEquals(1, entityManager.createQuery(query).getResultList().size());
    }

    @Test
    void testPersistTwo() {
        final CriteriaQuery<Book> query = entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        assertEquals(1, entityManager.createQuery(query).getResultList().size());
    }
}
