package io.micronaut.test.spock;

import javax.inject.Singleton;

@Singleton
interface MathService {

    Integer compute(Integer num);
}