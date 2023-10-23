package io.micronaut.test.spock

import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.Sql
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import javax.sql.DataSource

// tag::clazz[]
@MicronautTest
@Property(name = "datasources.default.dialect", value = "H2")
@Property(name = "datasources.default.driverClassName", value = "org.h2.Driver")
@Property(name = "datasources.default.schema-generate", value = "CREATE_DROP")
@Property(name = "datasources.default.url", value = "jdbc:h2:mem:devDb;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
@Property(name = "datasources.default.username", value = "sa")

@Sql(["classpath:create.sql", "classpath:datasource_1_insert.sql"]) // <1>
class SqlDatasourceSpec extends Specification {

    @Inject
    DataSource dataSource

    def "data is inserted"() {
        expect:
        readAllNames(dataSource) == ["Aardvark", "Albatross"]
    }

    List<String> readAllNames(DataSource dataSource) {
        dataSource.getConnection().withCloseable {
            it.prepareStatement("select name from MyTable").withCloseable {
                it.executeQuery().withCloseable {
                    def names = []
                    while (it.next()) {
                        names << it.getString(1)
                    }
                    names
                }
            }
        }
    }
}
// end::clazz[]
