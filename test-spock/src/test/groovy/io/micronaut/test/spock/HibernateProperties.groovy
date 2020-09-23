package io.micronaut.test.spock

import io.micronaut.context.annotation.Property
import io.micronaut.core.util.StringUtils

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.METHOD, ElementType.TYPE])
@Documented
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
@interface HibernateProperties {}