
package io.micronaut.test.junit;

import jakarta.inject.Singleton;

@Singleton
class MathServiceImpl implements MathService {

    @Override
    public Integer compute(Integer num) {
        return num * 4;
    }
}
