
package io.micronaut.test.kotlintest

import javax.inject.Singleton

@Singleton
internal class MathServiceImpl : MathService {

    override fun compute(num: Int): Int {
        return num * 4
    }
}
