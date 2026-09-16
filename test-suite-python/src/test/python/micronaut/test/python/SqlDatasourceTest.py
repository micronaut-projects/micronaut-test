from typing import Annotated

from jakarta.inject import Inject
from javax.sql import DataSource
from micronaut.context.annotation import Property
from micronaut.test.annotation import Sql, TransactionMode
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test


# tag::clazz[]
@MicronautTest(transactionMode=TransactionMode.SINGLE_TRANSACTION)
@Property(name="datasources.default.dialect", value="H2")
@Property(name="datasources.default.driverClassName", value="org.h2.Driver")
@Property(name="datasources.default.schema-generate", value="CREATE_DROP")
@Property(name="datasources.default.url", value="jdbc:h2:mem:SqlDatasourceTest;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
@Property(name="datasources.default.username", value="sa")
@Sql(["classpath:create.sql", "classpath:datasource_1_insert.sql"])  # <1>
class SqlDatasourceTest:

    data_source: Annotated[DataSource, Inject]

    @Test
    def data_is_inserted(self):
        assert ["Aardvark", "Albatross"] == self.read_all_names()

    def read_all_names(self) -> list[str]:
        result = []
        connection = self.data_source.getConnection()
        try:
            statement = connection.prepareStatement("select name from MyTable")
            result_set = statement.executeQuery()
            while result_set.next():
                result.append(result_set.getString(1))
        finally:
            connection.close()
        return result
# end::clazz[]
