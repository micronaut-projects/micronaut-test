package io.micronaut.test.kotest

import javax.inject.Singleton

@Singleton
internal class MathServiceImpl : MathService {

    override fun compute(num: Int): Int {
        return num * 4
    }
}
