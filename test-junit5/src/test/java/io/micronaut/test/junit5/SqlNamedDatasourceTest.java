package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.Sql;
import io.micronaut.test.annotation.TransactionMode;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(transactionMode = TransactionMode.SINGLE_TRANSACTION)

@Sql(dataSourceName = "one", value = {"classpath:create.sql", "classpath:datasource_1_insert.sql"})
@DbProperties
@Property(name = "datasources.one.dialect", value = "H2")
@Property(name = "datasources.one.driverClassName", value = "org.h2.Driver")
@Property(name = "datasources.one.schema-generate", value = "CREATE_DROP")
@Property(name = "datasources.one.url", value = "jdbc:h2:mem:databaseOne;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
@Property(name = "datasources.one.username", value = "sa")

@Sql(dataSourceName = "two", scripts = {"classpath:create.sql", "classpath:datasource_2_insert.sql"})
@Property(name = "datasources.two.dialect", value = "H2")
@Property(name = "datasources.two.driverClassName", value = "org.h2.Driver")
@Property(name = "datasources.two.schema-generate", value = "CREATE_DROP")
@Property(name = "datasources.two.url", value = "jdbc:h2:mem:databaseTwo;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
@Property(name = "datasources.two.username", value = "sa")
class SqlNamedDatasourceTest {

    @Inject
    @Named("one")
    DataSource dataSource1;

    @Inject
    @Named("two")
    DataSource dataSource2;

    @Test
    void dataIsInserted() throws Exception {
        assertEquals(List.of("Aardvark", "Albatross"), readAllNames(dataSource1));
        assertEquals(List.of("Bear", "Bumblebee"), readAllNames(dataSource2));
    }

    List<String> readAllNames(DataSource dataSource) throws SQLException {
        var result = new ArrayList<String>();
        try (
                Connection ds = dataSource.getConnection();
                PreparedStatement ps = ds.prepareStatement("select name from test");
                ResultSet rslt = ps.executeQuery()
        ) {
            while(rslt.next()) {
                result.add(rslt.getString(1));
            }
        }
        return result;
    }
}
