package io.micronaut.test.junit5;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.transaction.TransactionOperations;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import io.micronaut.transaction.annotation.Transactional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Data written once for the whole class belongs outside the per-test transaction, which is rolled
 * back after every test. A PER_CLASS {@code @BeforeAll} method can open its own transaction.
 */
@MicronautTest
@io.micronaut.context.annotation.Property(name = "datasources.default.url", value = "jdbc:h2:mem:BeforeAllTransactionTest;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
@DbProperties
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BeforeAllTransactionTest {

    @Inject
    EntityManager entityManager;

    @Inject
    TransactionOperations<?> transactionOperations;

    @BeforeAll
    @Transactional
    void insertSharedData() {
        assertTrue(transactionOperations.findTransactionStatus().isPresent(), "@BeforeAll runs in a transaction");
        Book book = new Book();
        book.setTitle("Shared by every test");
        entityManager.persist(book);
    }

    @AfterAll
    @Transactional
    void deleteSharedData() {
        assertTrue(transactionOperations.findTransactionStatus().isPresent(), "@AfterAll runs in a transaction");
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaDelete<Book> delete = criteriaBuilder.createCriteriaDelete(Book.class);
        delete.from(Book.class);
        entityManager.createQuery(delete).executeUpdate();
    }

    @Test
    void sharedDataIsVisible() {
        assertEquals(1, countBooks());
    }

    @Test
    void sharedDataSurvivesTheRollbackOfOtherTests() {
        Book book = new Book();
        book.setTitle("Rolled back after this test");
        entityManager.persist(book);
        assertEquals(2, countBooks());
    }

    private int countBooks() {
        CriteriaQuery<Book> query = entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        return entityManager.createQuery(query).getResultList().size();
    }
}
