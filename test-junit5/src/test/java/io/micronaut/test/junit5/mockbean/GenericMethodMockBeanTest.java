package io.micronaut.test.junit5.mockbean;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * An interface whose method declares its own bounded type variables can be mocked.
 */
@MicronautTest
@Property(name = "spec.name", value = "GenericMethodMockBeanTest")
class GenericMethodMockBeanTest {

    @Inject
    Dispatcher dispatcher;

    @Test
    void theMockIsUsed() {
        assertEquals("mocked", dispatcher.dispatch());
    }

    @MockBean(CommandBus.class)
    CommandBus commandBus() {
        return new CommandBus() {
            @SuppressWarnings("unchecked")
            @Override
            public <R, C extends Command<R>> R send(C command) {
                return (R) "mocked";
            }
        };
    }

    public interface Command<R> {
        R execute();
    }

    public interface CommandBus {
        <R, C extends Command<R>> R send(C command);
    }

    @Singleton
    @Requires(property = "spec.name", value = "GenericMethodMockBeanTest")
    static class DefaultCommandBus implements CommandBus {
        @Override
        public <R, C extends Command<R>> R send(C command) {
            return command.execute();
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = "GenericMethodMockBeanTest")
    static class Dispatcher {
        private final CommandBus commandBus;

        Dispatcher(CommandBus commandBus) {
            this.commandBus = commandBus;
        }

        String dispatch() {
            Command<String> command = () -> "real";
            return commandBus.send(command);
        }
    }
}
