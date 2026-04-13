package io.micronaut.test.junit5;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestPropertyProviderLifecycleValidationTest {

    @Test
    void testPropertyProviderRequiresPerClassLifecycle() throws Exception {
        ExtensionContext extensionContext = mock(ExtensionContext.class);
        doReturn(MisconfiguredTestPropertyProviderLifecycleSubject.class).when(extensionContext).getRequiredTestClass();
        when(extensionContext.getTestInstanceLifecycle()).thenReturn(Optional.of(TestInstance.Lifecycle.PER_METHOD));

        ExtensionConfigurationException e = assertThrows(
            ExtensionConfigurationException.class,
            () -> new MicronautJunit5Extension().beforeAll(extensionContext)
        );

        assertEquals("Tests that implement TestPropertyProvider must use the PER_CLASS test instance lifecycle.", e.getMessage());
    }

    @MicronautTest
    static class MisconfiguredTestPropertyProviderLifecycleSubject implements TestPropertyProvider {

        @Override
        public @NonNull Map<String, String> getProperties() {
            return Map.of("foo.bar", "one");
        }
    }
}
