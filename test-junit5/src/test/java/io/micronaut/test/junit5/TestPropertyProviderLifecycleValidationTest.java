package io.micronaut.test.junit5;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestPropertyProviderLifecycleValidationTest {

    @Test
    void testPropertyProviderRequiresPerClassLifecycle() throws Exception {
        ExtensionContext extensionContext = extensionContext(
            MisconfiguredTestPropertyProviderLifecycleSubject.class,
            TestInstance.Lifecycle.PER_METHOD
        );

        ExtensionConfigurationException e = assertThrows(
            ExtensionConfigurationException.class,
            () -> new MicronautJunit5Extension().beforeAll(extensionContext)
        );

        assertEquals("Tests that implement TestPropertyProvider must use the PER_CLASS test instance lifecycle.", e.getMessage());
    }

    private static ExtensionContext extensionContext(Class<?> testClass, TestInstance.Lifecycle lifecycle) {
        return (ExtensionContext) Proxy.newProxyInstance(
            ExtensionContext.class.getClassLoader(),
            new Class<?>[]{ExtensionContext.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "getRequiredTestClass" -> testClass;
                case "getTestInstanceLifecycle" -> Optional.of(lifecycle);
                case "toString" -> "TestExtensionContext[" + testClass.getName() + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException("Unexpected ExtensionContext method: " + method.getName());
            }
        );
    }

    @MicronautTest
    static class MisconfiguredTestPropertyProviderLifecycleSubject implements TestPropertyProvider {

        @Override
        public @NonNull Map<String, String> getProperties() {
            return Map.of("foo.bar", "one");
        }
    }
}
