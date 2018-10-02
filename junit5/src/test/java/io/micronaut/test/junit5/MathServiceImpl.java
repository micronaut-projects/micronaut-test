package io.micronaut.test.junit5;

import javax.inject.Singleton;

@Singleton
class MathServiceImpl implements MathService {

    @Override
    public Integer compute(Integer num) {
        throw new UnsupportedOperationException("The method compute should be never called :-)");
    }
}
