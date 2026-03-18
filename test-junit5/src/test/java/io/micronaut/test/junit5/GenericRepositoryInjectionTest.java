package io.micronaut.test.junit5;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Property(name = "spec.name", value = "GenericRepositoryInjectionTest")
@MicronautTest
class GenericRepositoryInjectionTest {

    @Inject
    private RepositoryContract<String, String> stringRepository;

    @Inject
    private RepositoryContract<Long, Long> longRepository;

    @Test
    void injectsGenericRepositories() {
        assertNotNull(stringRepository);
        assertNotNull(longRepository);
    }

    interface RepositoryContract<K, V> {
        String get(K key, V value);
    }

    @Singleton
    @Secondary
    @Requires(property = "spec.name", value = "GenericRepositoryInjectionTest")
    static class StringRepository implements RepositoryContract<String, String> {
        @Override
        public String get(String key, String value) {
            return key + value;
        }
    }

    @Singleton
    @Secondary
    @Requires(property = "spec.name", value = "GenericRepositoryInjectionTest")
    static class LongRepository implements RepositoryContract<Long, Long> {
        @Override
        public String get(Long key, Long value) {
            return key + ":" + value;
        }
    }
}
