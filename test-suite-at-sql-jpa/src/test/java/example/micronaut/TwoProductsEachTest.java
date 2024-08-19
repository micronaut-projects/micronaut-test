package example.micronaut;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.test.annotation.Sql;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Sql(scripts = "classpath:tworandomproducts.sql", phase = Sql.Phase.BEFORE_EACH)
@Sql(scripts = "classpath:rollbackallproducts.sql", phase = Sql.Phase.AFTER_ALL)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers(disabledWithoutDocker = true)
class TwoProductsEachTest implements TestPropertyProvider {

    @Override
    public @NonNull Map<String, String> getProperties() {
        return PostgreSQL.getProperties();
    }

    @Test
    @Order(1)
    void thereAreTwoProducts(ProductRepository productRepository) {
        assertEquals(2L, productRepository.count());
    }

    @Test
    @Order(2)
    void thereAreThenFourProducts(ProductRepository productRepository) {
        assertEquals(4L, productRepository.count());
    }
}
