package io.micronaut.test.junit5;

import io.micronaut.aop.InterceptedProxy;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.runtime.context.scope.Refreshable;
import io.micronaut.runtime.context.scope.refresh.RefreshEvent;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Only the bean selected for an injection point may be unwrapped: another candidate being a mock
 * must leave the proxy of the real bean that was injected in place.
 */
@Property(name = "spec.name", value = MixedMockCandidatesTest.SPEC)
@MicronautTest
@DisabledInNativeImage
class MixedMockCandidatesTest {

    static final String SPEC = "MixedMockCandidatesTest";

    @Inject
    ApplicationContext applicationContext;

    @Inject
    Foo foo;

    @Inject
    @Named("stub")
    Foo stub;

    @Inject
    Store<String> mockedStore;

    @Inject
    Store<Long> realStore;

    private Foo fooFromSetter;

    @Inject
    void setFooFromSetter(Foo fooFromSetter) {
        this.fooFromSetter = fooFromSetter;
    }

    @MockBean
    @Named("stub")
    Foo stubFoo() {
        return Mockito.mock(Foo.class);
    }

    @MockBean
    Store<String> stringStore() {
        return Mockito.mock(Store.class);
    }

    @Test
    void realScopedBeanKeepsItsProxyAndRefreshes() {
        assertInstanceOf(InterceptedProxy.class, foo);
        assertInstanceOf(InterceptedProxy.class, fooFromSetter);

        String before = foo.id();
        applicationContext.publishEvent(new RefreshEvent());

        assertNotEquals(before, foo.id(), "the refresh scope was bypassed");
    }

    @Test
    void selectedMockIsStillUnwrapped() {
        assertTrue(Mockito.mockingDetails(stub).isMock());
        assertTrue(Mockito.mockingDetails(mockedStore).isMock());
        assertFalse(Mockito.mockingDetails(realStore).isMock());
    }

    interface Foo {
        String id();
    }

    interface Store<T> {
        T get();
    }

    @Primary
    @Named("real")
    @Refreshable
    @Requires(property = "spec.name", value = SPEC)
    static class RealFoo implements Foo {
        private final String id = UUID.randomUUID().toString();

        @Override
        public String id() {
            return id;
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC)
    static class LongStore implements Store<Long> {
        @Override
        public Long get() {
            return 1L;
        }
    }
}
