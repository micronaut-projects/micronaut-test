package io.micronaut.test.junit5.rebuild;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Other extensions that read the application context from the JUnit store see the rebuilt one.
 */
@MicronautTest(rebuildContext = true)
@ExtendWith(RebuildContextExtensionStoreTest.StoreReader.class)
class RebuildContextExtensionStoreTest {

    @Inject
    ApplicationContext applicationContext;

    @Test
    void firstTest() {
        assertSame(applicationContext, StoreReader.seen);
    }

    @Test
    void secondTest() {
        assertSame(applicationContext, StoreReader.seen);
    }

    static class StoreReader implements BeforeEachCallback {
        static ApplicationContext seen;

        @Override
        public void beforeEach(ExtensionContext context) {
            seen = context.getRoot()
                .getStore(ExtensionContext.Namespace.create(MicronautJunit5Extension.class))
                .get(ApplicationContext.class, ApplicationContext.class);
        }
    }
}
