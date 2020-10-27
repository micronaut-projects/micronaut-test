
package io.micronaut.test.junit5;

import io.micronaut.context.DefaultApplicationContextBuilder;
import io.micronaut.core.annotation.Introspected;

import java.util.Collections;

@Introspected
public class CustomContextBuilder extends DefaultApplicationContextBuilder {
    public CustomContextBuilder() {
        properties(Collections.singletonMap(
                "custom.builder.prop", "value"
        ));
    }
}
