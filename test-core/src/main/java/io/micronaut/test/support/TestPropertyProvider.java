package io.micronaut.test.support;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Allows providing application properties dynamically from a test.
 *
 * @author graemerocher
 * @since 1.1.0
 */
@FunctionalInterface
public interface TestPropertyProvider extends Supplier<Map<String, String>> {

    @Override
    default Map<String, String> get() {
        return getProperties();
    }

    /**
     * Allows dynamically providing properties for a test.
     * @return A map of properties
     */
    @Nonnull
    Map<String, String> getProperties();
}
