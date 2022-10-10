
package io.micronaut.test.spock.entities

//import grails.gorm.annotation.Entity
import io.micronaut.core.annotation.Introspected
//import org.grails.datastore.gorm.GormEntity

@Introspected
//@Entity
class Book { // implements GormEntity<Book> {
    String name

    static constraints = {
        name nullable: false, size: 1..100
    }
}
