package example.micronaut;

import example.micronaut.entities.Product;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.test.annotation.Sql;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Sql("classpath:threeproducts.sql")
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ThreeProductsTest implements TestPropertyProvider {
    @Override
    public @NonNull Map<String, String> getProperties() {
        return PostgreSQL.getProperties();
    }

    @Test
    void thereAreTwoProducts(ProductRepository productRepository) {
        assertEquals(3L, productRepository.count());
        productRepository.save(new Product(5L, "foo", "bar"));
    }
}
