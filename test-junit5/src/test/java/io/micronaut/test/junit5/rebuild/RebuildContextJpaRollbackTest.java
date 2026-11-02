package io.micronaut.test.junit5.rebuild;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.junit5.Book;
import io.micronaut.test.junit5.DbProperties;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The test transaction is opened and rolled back on the datasource of each rebuilt context.
 */
@MicronautTest(rebuildContext = true)
@io.micronaut.context.annotation.Property(name = "datasources.default.url", value = "jdbc:h2:mem:RebuildContextJpaRollbackTest;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
@DbProperties
class RebuildContextJpaRollbackTest {

    @Inject
    EntityManager entityManager;

    @BeforeEach
    void insertInSetup() {
        Book book = new Book();
        book.setTitle("From setup");
        entityManager.persist(book);
    }

    @Test
    void firstTest() {
        persistAndCount();
    }

    @Test
    void secondTest() {
        persistAndCount();
    }

    private void persistAndCount() {
        Book book = new Book();
        book.setTitle("From the test");
        entityManager.persist(book);
        CriteriaQuery<Book> query = entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        assertEquals(2, entityManager.createQuery(query).getResultList().size());
    }
}
