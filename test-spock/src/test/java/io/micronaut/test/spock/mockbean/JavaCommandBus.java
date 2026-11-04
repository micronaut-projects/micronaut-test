package io.micronaut.test.spock.mockbean;

public interface JavaCommandBus {
    <R, C extends JavaCommand<R>> R send(C command);
}
