package io.micronaut.test.r2dbc;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.Sql;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.r2dbc.spi.ConnectionFactory;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertEquals;

// tag::clazz[]
@MicronautTest
@Property(name = "r2dbc.datasources.default.db-type", value = "mysql")
@Sql(value = {"classpath:create.sql", "classpath:datasource_1_insert.sql"}, resourceType = ConnectionFactory.class)
class MySqlConnectionTest  {

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
