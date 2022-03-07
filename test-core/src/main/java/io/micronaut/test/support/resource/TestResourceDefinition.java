package io.micronaut.test.support.resource;

import java.util.Optional;

/**
 * The resource definition. Currently just exposes the name, but
 * future versions may expose further information.
 *
 * @author graemerocher
 * @since 3.1.0
 */
public interface TestResourceDefinition {
    /**
     * @return The name of the definition.
     */
    Optional<String> getName();
}
