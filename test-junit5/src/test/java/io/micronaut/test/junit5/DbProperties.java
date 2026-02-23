
package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Documented

@Property(name = "datasources.default.name", value = "testdb")
@Property(name = "jpa.default.properties.hibernate.hbm2ddl.auto", value = "update")
@Property(name = "jpa.default.compileTimeHibernateProxies", value = "true")
public @interface DbProperties {
}
