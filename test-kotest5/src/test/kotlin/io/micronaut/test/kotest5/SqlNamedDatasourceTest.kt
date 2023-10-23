package io.micronaut.test.kotest5

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.Sql
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import jakarta.inject.Named
import javax.sql.DataSource

@DbProperties

// tag::clazz[]
@MicronautTest
@Sql(dataSourceName = "one", value = ["classpath:create.sql", "classpath:datasource_1_insert.sql"]) // <1>
@Property(name = "datasources.one.dialect", value = "H2")
@Property(name = "datasources.one.driverClassName", value = "org.h2.Driver")
@Property(name = "datasources.one.schema-generate", value = "CREATE_DROP")
@Property(name = "datasources.one.url", value = "jdbc:h2:mem:devDb;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
@Property(name = "datasources.one.username", value = "sa")

@Sql(dataSourceName = "two", value = ["classpath:create.sql", "classpath:datasource_2_insert.sql"]) // <1>
@Property(name = "datasources.two.dialect", value = "H2")
@Property(name = "datasources.two.driverClassName", value = "org.h2.Driver")
@Property(name = "datasources.two.schema-generate", value = "CREATE_DROP")
@Property(name = "datasources.two.url", value = "jdbc:h2:mem:devDb2;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
@Property(name = "datasources.two.username", value = "sa")
class SqlNamedDatasourceTest(
    @Named("one") private val dataSource1: DataSource,
    @Named("two") private val dataSource2: DataSource
): BehaviorSpec({

    fun readAllNames(dataSource: DataSource): List<String> {
        val result = mutableListOf<String>()
        println("Testing datasource: $dataSource")
        dataSource.connection.use { ds ->
            ds.prepareStatement("select name from MyTable").use { ps ->
                ps.executeQuery().use { rslt ->
                    while (rslt.next()) {
                        result.add(rslt.getString(1))
                    }
                }
            }
        }
        return result
    }

    given("a test with the Sql annotation") {
        then("the data is inserted as expected") {
            readAllNames(dataSource1) shouldBe listOf("Aardvark", "Albatross")
            readAllNames(dataSource2) shouldBe listOf("Bear", "Bumblebee")
        }
    }
})
// end::clazz[]
