
package io.micronaut.test.junit5;

import jakarta.inject.Singleton;

@Singleton
class MathServiceImpl implements MathService {

    @Override
    public Integer compute(Integer num) {
        return num * 4;
    }
}
