package example.micronaut;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.test.annotation.Sql;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Sql(scripts = "classpath:twoproducts.sql")
@Sql(scripts = "classpath:rollbacktwoproducts.sql", phase = Sql.Phase.AFTER_EACH)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TwoProductsThenNoneTest implements TestPropertyProvider {

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
    void thereAreThenZeroProducts(ProductRepository productRepository) {
        assertEquals(0L, productRepository.count());
    }
}
