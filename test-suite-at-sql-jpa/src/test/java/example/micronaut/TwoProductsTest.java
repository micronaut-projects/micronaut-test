package example.micronaut;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.test.annotation.Sql;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Sql(scripts = "classpath:twoproducts.sql")
@Sql(scripts = "classpath:rollbacktwoproducts.sql", phase = Sql.Phase.AFTER_ALL)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers(disabledWithoutDocker = true)
class TwoProductsTest implements TestPropertyProvider {

    @Override
    public @NonNull Map<String, String> getProperties() {
        return PostgreSQL.getProperties();
    }

    @Test
    void thereAreTwoProducts(ProductRepository productRepository) {
        assertEquals(2L, productRepository.count());
    }
}
