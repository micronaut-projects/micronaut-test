package io.micronaut.test.spock

import javax.inject.Singleton

@Singleton
class MathServiceImpl implements MathService {

    @Override
    Integer compute(Integer num) {
        return num * 4 // should never be called
    }
}