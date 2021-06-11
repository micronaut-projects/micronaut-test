package io.micronaut.test.junit5;

import jakarta.inject.Singleton;

@Singleton
public class SimpleService {

    private SimpleWorker simpleWorker;

    public SimpleService(SimpleWorker simpleWorker) {
        this.simpleWorker = simpleWorker;
    }

    public SimpleWorker getSimpleWorker() {
        return simpleWorker;
    }
}
