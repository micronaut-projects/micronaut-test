package io.micronaut.test.junit5;

import javax.inject.Singleton;

@Singleton
public class SimpleService implements SimpleServiceInterface {

    private SimpleWorker simpleWorker;

    public SimpleService(SimpleWorker simpleWorker) {
        this.simpleWorker = simpleWorker;
    }

    public SimpleWorker getSimpleWorker() {
        return simpleWorker;
    }
}
