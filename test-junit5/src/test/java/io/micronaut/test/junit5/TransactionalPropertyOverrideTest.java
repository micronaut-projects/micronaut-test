package io.micronaut.test.junit5;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.micronaut.transaction.test.DefaultTestTransactionExecutionListener;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Collections;
import java.util.Map;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionalPropertyOverrideTest implements TestPropertyProvider {

    @Inject
    ApplicationContext applicationContext;

    @Override
    public Map<String, String> getProperties() {
        return Collections.singletonMap("micronaut.test.transactional", "false");
    }

    @Test
    void transactionalDisabledByProperty() {
        Assertions.assertFalse(applicationContext.containsBean(DefaultTestTransactionExecutionListener.class));
        Assertions.assertFalse(applicationContext.getEnvironment().getProperty("micronaut.test.transactional", Boolean.class).orElse(true));
    }
}
