package io.micronaut.test.r2dbc;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.Sql;
import io.micronaut.test.extensions.junit.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.r2dbc.spi.ConnectionFactory;
import jakarta.inject.Inject;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

// tag::clazz[]
@MicronautTest
@Property(name = "r2dbc.datasources.default.db-type", value = "mysql")
@Sql(value = {"classpath:create.sql", "classpath:datasource_1_insert.sql"}, resourceType = ConnectionFactory.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers(disabledWithoutDocker = true)
class MySqlConnectionTest implements TestPropertyProvider {
    @Override
    @NonNull
    public Map<String, String> getProperties() {
        return MySQL.getProperties();
    }

    @Inject
    ConnectionFactory connectionFactory;

    @Test
    void testSqlHasBeenInjected() {
        var f = Flux.from(connectionFactory.create());

        var result = f.flatMap(connection ->
            connection.createStatement("SELECT name from MyTable where id = 2").execute()
        ).flatMap(rslt ->
            rslt.map((row, metadata) -> row.get(0, String.class))
        ).blockFirst();

        assertEquals("Albatross", result);
    }
}
// end::clazz[]
