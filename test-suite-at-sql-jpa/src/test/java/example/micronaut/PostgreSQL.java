package example.micronaut;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.test.support.TestPropertyProvider;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

public class PostgreSQL {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        "postgres:15.2-alpine"
    );

    public static @NonNull Map<String, String> getProperties() {
        if (!postgres.isRunning()) {
            postgres.start();
        }
        return Map.of(
            "jpa.default.entity-scan.packages", "example.micronaut.entities",
            "jpa.default.properties.hibernate.hbm2ddl.auto", "update",
            "datasources.default.db-type", "postgres",
            "datasources.default.dialect", "POSTGRES",
            "datasources.default.driver-class-name", "org.postgresql.Driver",
            "datasources.default.url", postgres.getJdbcUrl(),
            "datasources.default.username", postgres.getUsername(),
            "datasources.default.password", postgres.getPassword());
    }
}
