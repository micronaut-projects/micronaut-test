package io.micronaut.test.junit5.lifecycle;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Reads the context from the store the Micronaut extension keeps it in, after that extension has
 * finished with the class.
 */
// registered before the Micronaut extension, so its afterAll runs after that extension's
@ExtendWith(ContextStoreFixture.ReadStoreAfterAll.class)
@MicronautTest
@Tag(LifecycleFixtures.TAG)
class ContextStoreFixture {

    static final AtomicReference<Object> AFTER_ALL = new AtomicReference<>();

    @Test
    void contextIsInTheStore(ApplicationContext context) {
        assertNotNull(context);
    }

    static class ReadStoreAfterAll implements AfterAllCallback {
        @Override
        public void afterAll(ExtensionContext context) {
            AFTER_ALL.set(new StoreAccess().store(context).get(ApplicationContext.class));
        }
    }

    private static class StoreAccess extends MicronautJunit5Extension {
        ExtensionContext.Store store(ExtensionContext context) {
            return getStore(context);
        }
    }
}
