package io.micronaut.test.junit5.parallel;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.Sql;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@code @Sql} scripts for the {@code BEFORE_EACH} and {@code AFTER_EACH} phases run against the one
 * datasource of the shared context, so overlapping methods would insert into each other's fixtures.
 */
@MicronautTest
@Tag(ParallelFixtures.TAG)
@Property(name = "datasources.default.dialect", value = "H2")
@Property(name = "datasources.default.driverClassName", value = "org.h2.Driver")
@Property(name = "datasources.default.username", value = "sa")
@Property(name = "datasources.default.url",
    value = "jdbc:h2:mem:SqlPhaseFixture;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
@Sql(value = "classpath:parallel/create.sql", phase = Sql.Phase.BEFORE_ALL)
@Sql(value = "classpath:parallel/insert.sql", phase = Sql.Phase.BEFORE_EACH)
@Sql(value = "classpath:parallel/delete.sql", phase = Sql.Phase.AFTER_EACH)
class SqlPhaseFixture {

    @Inject
    DataSource dataSource;

    @Test
    void one() throws SQLException {
        assertExactlyOneRow();
    }

    @Test
    void two() throws SQLException {
        assertExactlyOneRow();
    }

    @Test
    void three() throws SQLException {
        assertExactlyOneRow();
    }

    @Test
    void four() throws SQLException {
        assertExactlyOneRow();
    }

    private void assertExactlyOneRow() throws SQLException {
        assertEquals(1, countRows(), "another test inserted into the shared datasource");
        assertEquals(1, ConcurrencyRecorder.hold("sql-phase", 30), ParallelFixtures.NO_OVERLAP);
        assertEquals(1, countRows(), "another test inserted into the shared datasource");
    }

    private int countRows() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("select count(*) from ParallelRow");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
}
