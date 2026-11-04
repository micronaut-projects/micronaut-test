package io.micronaut.test.junit5.rebuild;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * The method properties of the first test stop its rebuilt context from starting, and must not be
 * carried over to the next test.
 */
@MicronautTest(rebuildContext = true)
@Tag(RebuildContextFailedStartPropertyTest.TAG)
@Property(name = "spec.name", value = RebuildContextFailedStartPropertyFixture.SPEC)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RebuildContextFailedStartPropertyFixture {

    static final String SPEC = "RebuildContextFailedStartPropertyFixture";
    static final String FAIL_STARTUP = "rebuild.fail-startup";

    @Inject
    ApplicationContext applicationContext;

    @Test
    @Order(1)
    @Property(name = FAIL_STARTUP, value = "true")
    void contextFailsToStart() {
    }

    @Test
    @Order(2)
    void nextTestDoesNotSeeTheProperty() {
        assertFalse(applicationContext.containsProperty(FAIL_STARTUP));
    }

    @Context
    @Requires(property = "spec.name", value = SPEC)
    @Requires(property = FAIL_STARTUP, value = "true")
    static class FailingBean {
        FailingBean() {
            throw new IllegalStateException("startup failure");
        }
    }
}
