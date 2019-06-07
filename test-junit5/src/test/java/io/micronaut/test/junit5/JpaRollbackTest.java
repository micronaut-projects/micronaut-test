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

    @Inject
    PlatformTransactionManager transactionManager;

    @BeforeEach
    void setup() {
        final TransactionStatus tx = transactionManager.getTransaction(new DefaultTransactionDefinition());
        final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        final CriteriaDelete<Book> delete = criteriaBuilder.createCriteriaDelete(Book.class);
        delete.from(Book.class);
        entityManager.createQuery(delete).executeUpdate();
        transactionManager.commit(tx);
    }

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
