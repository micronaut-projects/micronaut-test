
package io.micronaut.test.kotest5

import jakarta.inject.Singleton

@Singleton
internal class MathServiceImpl : MathService {

    override fun compute(num: Int): Int {
        return num * 4
    }
}
