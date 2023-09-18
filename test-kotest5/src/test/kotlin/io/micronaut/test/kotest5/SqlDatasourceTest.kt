package io.micronaut.test.kotest5

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.Sql
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import javax.sql.DataSource

@MicronautTest
@Sql("classpath:data.sql", "classpath:data1.sql")
@DbProperties
@Property(name = "datasources.default.dialect", value = "H2")
@Property(name = "datasources.default.driverClassName", value = "org.h2.Driver")
@Property(name = "datasources.default.schema-generate", value = "CREATE_DROP")
@Property(name = "datasources.default.url", value = "jdbc:h2:mem:devDb;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
@Property(name = "datasources.default.username", value = "sa")
class SqlDatasourceTest(
    private val dataSource: DataSource
): BehaviorSpec({

    fun readAllNames(dataSource: DataSource): List<String> {
        val result = mutableListOf<String>()
        dataSource.connection.use { ds ->
            ds.prepareStatement("select name from test").use { ps ->
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
            readAllNames(dataSource) shouldBe listOf("Aardvark", "Albatross")
        }
    }
})
