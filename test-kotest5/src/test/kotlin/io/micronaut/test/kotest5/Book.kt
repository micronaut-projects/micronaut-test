package io.micronaut.test.kotest5

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity
class Book {

    @GeneratedValue
    @Id
    var id: Long? = null

    var title: String? = null
}
