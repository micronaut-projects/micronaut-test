
package io.micronaut.test.spock.entities

import grails.gorm.annotation.Entity
import org.grails.datastore.gorm.GormEntity

@Entity
class Book implements GormEntity<Book> {
    String name

    static constraints = {
        name nullable: false, size: 1..100
    }
}
