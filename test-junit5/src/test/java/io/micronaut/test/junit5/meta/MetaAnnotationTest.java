package io.micronaut.test.junit5.meta;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.AbstractMicronautExtension;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@NonTransactionalMicronautTest
class MetaAnnotationTest {

    @Inject
    ApplicationContext applicationContext;

    @Test
    void attributesOfTheMetaAnnotationApply() {
        assertTrue(applicationContext.getEnvironment().getActiveNames().contains("meta"));
        assertEquals(Boolean.FALSE, applicationContext.getProperty(AbstractMicronautExtension.TEST_TRANSACTIONAL, Boolean.class).orElse(null));
    }
}
