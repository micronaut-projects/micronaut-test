package example.micronaut;

import example.micronaut.entities.Product;
import io.micronaut.context.annotation.Property;
import io.micronaut.data.connection.ConnectionOperations;
import io.micronaut.data.connection.annotation.Connectable;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.transaction.TransactionOperations;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;

@H2DBProperties
@MicronautTest(startApplication = false, transactional = false)
@Property(name = "datasources.default.allow-connection-per-operation", value = "false")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InterceptedTest {

    @Inject
    ConnectionOperations<Connection> connectionOperations;

    @Inject
    TransactionOperations<Connection> transactionOperations;

    @Inject
    ProductRepository productRepository;

    @Connectable
    @BeforeEach
    void setup(ProductRepository productRepository) {
        Assertions.assertTrue(connectionOperations.findConnectionStatus().isPresent());
        productRepository.deleteAll();
    }

    @Connectable
    @AfterEach
    void cleanup(ProductRepository productRepository) {
        Assertions.assertTrue(connectionOperations.findConnectionStatus().isPresent());
        productRepository.deleteAll();
    }

    @Test
    @Connectable
    void thereAreTwoProducts(ProductRepository productRepository) {
        Assertions.assertTrue(connectionOperations.findConnectionStatus().isPresent());
        assertEquals(0L, productRepository.count());
        productRepository.save(new Product(999L, "foo", "bar"));
        assertEquals(1L, productRepository.count());
    }

    @Connectable
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testWithDifferentData(boolean data) {
        // Just test template invocation context works
        Assertions.assertTrue(connectionOperations.findConnectionStatus().isPresent());
        assertEquals(0L, productRepository.count());
        productRepository.save(new Product(999L, "foo", "bar"));
        assertEquals(1L, productRepository.count());
    }

    @Test
    void noConnectable() {
        Assertions.assertTrue(connectionOperations.findConnectionStatus().isEmpty());
    }

    @Connectable
    @Test
    void testConnection() {
        Assertions.assertTrue(connectionOperations.findConnectionStatus().isPresent());
        Assertions.assertTrue(transactionOperations.findTransactionStatus().isEmpty());
    }

    @Transactional
    @Test
    void testTX() {
        Assertions.assertTrue(connectionOperations.findConnectionStatus().isPresent());
        Assertions.assertTrue(transactionOperations.findTransactionStatus().isPresent());
    }
}

