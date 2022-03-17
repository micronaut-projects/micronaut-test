
package io.micronaut.test.kotest

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Book {

    @GeneratedValue
    @Id
    var id: Long? = null

    var title: String? = null
}
