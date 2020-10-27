
package io.micronaut.test.kotest

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.PropertySource

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.FILE)
@MustBeDocumented
@PropertySource(
        Property(name = "datasources.default.name", value = "testdb"),
        Property(name = "jpa.default.properties.hibernate.hbm2ddl.auto", value = "update")
)
annotation class DbProperties
