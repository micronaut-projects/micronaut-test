package io.micronaut.test.spock

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.core.util.StringUtils
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.spock.entities.Book
import io.micronaut.test.transaction.spring.SpringTransactionTestExecutionListener
import spock.lang.Specification
import spock.lang.Stepwise

import javax.inject.Inject

@MicronautTest(transactional = true, rollback = true, packages = "io.micronaut.test.spock.entities")
@Property(name = "hibernate.hbm2ddl.auto", value = "update")
@Property(name = "hibernate.cache.queries", value = StringUtils.FALSE)
@Property(name = "hibernate.cache.use_second_level_cache", value = StringUtils.FALSE)
@Property(name = "hibernate.cache.use_query_cache", value = StringUtils.FALSE)
@Property(name = "hibernate.dataSource.url", value = 'jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE')
@Property(name = "hibernate.dataSource.pooled", value = StringUtils.TRUE)
@Property(name = "hibernate.dataSource.jmxExport", value = StringUtils.TRUE)
@Property(name = "hibernate.dataSource.driverClassName", value = 'org.h2.Driver')
@Property(name = "hibernate.dataSource.username", value = 'sa')
@Property(name = "hibernate.dataSource.password", value = '')
@Stepwise
class GormTransactionalRollbackSpec extends Specification {

    @Inject
    ApplicationContext applicationContext

    void "bean SpringTransactionTestExecutionListener exists"() {
        expect:
        applicationContext.containsBean(SpringTransactionTestExecutionListener)
    }

    void "save book"() {
        when:
        new Book(name: "BAR").save(failOnError: true, flush: true)

        then:
        noExceptionThrown()

        and:
        Book.count() == old(Book.count()) + 1
    }

    void "book was rollbacked"() {
        expect:
        Book.count() == 0
    }
}
