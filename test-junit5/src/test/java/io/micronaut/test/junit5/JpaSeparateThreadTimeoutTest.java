package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.TransactionMode;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit runs the body of a {@code SEPARATE_THREAD} timeout on a thread of its own. Its built-in
 * timeout interceptor is registered before the declarative Micronaut extension and therefore wraps
 * it, so the Micronaut interceptor chain - and with it the test transaction - runs on that same
 * thread. This test pins that ordering down: a change to it would silently take transactional
 * isolation away from every timed test.
 */
@MicronautTest(transactionMode = TransactionMode.SINGLE_TRANSACTION)
@DbProperties
@Property(name = "datasources.default.url",
    value = "jdbc:h2:mem:JpaSeparateThreadTimeoutTest;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
class JpaSeparateThreadTimeoutTest {

    @Inject
    EntityManager entityManager;

    private String setUpThread;

    @BeforeEach
    void setUp() {
        setUpThread = Thread.currentThread().getName();
        Book book = new Book();
        book.setTitle("written by setup");
        entityManager.persist(book);
    }

    @Test
    void sameThreadSeesTheSetupTransaction() {
        assertEquals(setUpThread, Thread.currentThread().getName());
        assertTrue(entityManager.isJoinedToTransaction());
        assertEquals(1, countBooks());
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void separateTimeoutThreadSeesTheSameTransaction() {
        assertNotEquals(setUpThread, Thread.currentThread().getName(),
            "JUnit did not move the test body onto its own timeout thread, so this test proves nothing");
        assertTrue(entityManager.isJoinedToTransaction(),
            "the test transaction did not reach the timeout thread");
        assertEquals(1, countBooks(),
            "the timeout thread ran outside the setup transaction");
    }

    private int countBooks() {
        CriteriaQuery<Book> query = entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        return entityManager.createQuery(query).getResultList().size();
    }
}
