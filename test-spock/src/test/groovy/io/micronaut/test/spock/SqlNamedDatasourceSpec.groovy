package io.micronaut.test.spock

import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.Sql
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Named
import spock.lang.Specification

import javax.sql.DataSource

@MicronautTest

@Sql(dataSourceName = "one", value = ["classpath:create.sql", "classpath:datasource_1_insert.sql"])
@Property(name = "datasources.one.dialect", value = "H2")
@Property(name = "datasources.one.driverClassName", value = "org.h2.Driver")
@Property(name = "datasources.one.schema-generate", value = "CREATE_DROP")
@Property(name = "datasources.one.url", value = "jdbc:h2:mem:devDb;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
@Property(name = "datasources.one.username", value = "sa")

@Sql(dataSourceName = "two", value = ["classpath:create.sql", "classpath:datasource_2_insert.sql"])
@Property(name = "datasources.two.dialect", value = "H2")
@Property(name = "datasources.two.driverClassName", value = "org.h2.Driver")
@Property(name = "datasources.two.schema-generate", value = "CREATE_DROP")
@Property(name = "datasources.two.url", value = "jdbc:h2:mem:devDb2;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
@Property(name = "datasources.two.username", value = "sa")
class SqlNamedDatasourceSpec extends Specification {

    @Inject
    @Named("one")
    DataSource dataSource1

    @Inject
    @Named("two")
    DataSource dataSource2

    def "data is inserted"() {
        expect:
        readAllNames(dataSource1) == ["Aardvark", "Albatross"]

        and:
        readAllNames(dataSource2) == ["Bear", "Bumblebee"]
    }

    List<String> readAllNames(DataSource dataSource) {
        dataSource.getConnection().withCloseable {
            it.prepareStatement("select name from test").withCloseable {
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
