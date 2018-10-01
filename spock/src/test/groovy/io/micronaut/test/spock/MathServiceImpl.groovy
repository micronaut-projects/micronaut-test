package io.micronaut.test.spock

import javax.inject.Singleton

@Singleton
class MathServiceImpl implements MathService {

    @Override
    Integer compute(Integer num) {
        throw new UnsupportedOperationException("The method compute should be never called :-)")
    }
}