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
 * A @MockBean can supply a bean that has no implementation in the application.
 */
@MicronautTest
@Property(name = "spec.name", value = "MockOnlyBeanTest")
class MockOnlyBeanTest {

    @Inject
    RemoteUser remoteUser;

    @Test
    void theMockSuppliesTheMissingBean() {
        assertEquals("mock user", remoteUser.name());
    }

    @MockBean
    UserDirectory userDirectory() {
        return () -> "mock user";
    }

    public interface UserDirectory {
        String currentUser();
    }

    @Singleton
    @Requires(property = "spec.name", value = "MockOnlyBeanTest")
    static class RemoteUser {
        private final UserDirectory directory;

        RemoteUser(UserDirectory directory) {
            this.directory = directory;
        }

        String name() {
            return directory.currentUser();
        }
    }
}
