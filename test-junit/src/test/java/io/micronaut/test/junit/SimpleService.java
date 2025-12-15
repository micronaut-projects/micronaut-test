package io.micronaut.test.junit;

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
