
package io.micronaut.test.junit5;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.annotation.TransactionMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(transactionMode = TransactionMode.SINGLE_TRANSACTION)
@DbProperties
@Disabled("Disabled until we get a working micronaut-data for 4.0.0")
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
