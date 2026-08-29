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

@MicronautTest(transactional = true)
@io.micronaut.context.annotation.Property(name = "datasources.default.url", value = "jdbc:h2:mem:TransactionalDefaultPropertyExplicitTest;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
@DbProperties
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionalDefaultPropertyExplicitTest implements TestPropertyProvider {

    @Inject
    ApplicationContext applicationContext;

    @Override
    public Map<String, String> getProperties() {
        return Collections.singletonMap("micronaut.test.transactional-default", "false");
    }

    @Test
    void transactionalEnabledWhenExplicit() {
        Assertions.assertTrue(applicationContext.containsBean(DefaultTestTransactionExecutionListener.class));
        Assertions.assertTrue(applicationContext.getEnvironment()
            .getProperty("micronaut.test.transactional", Boolean.class)
            .orElse(false));
    }
}
