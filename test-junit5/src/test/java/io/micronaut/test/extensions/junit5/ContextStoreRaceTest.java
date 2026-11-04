package io.micronaut.test.extensions.junit5;

import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Classes running in parallel share the root store. One class publishing its context while another
 * removes its own, between that class's lookup and removal, must not lose the published context.
 */
@DisabledInNativeImage
class ContextStoreRaceTest {

    @Test
    void aContextPublishedDuringRemovalIsKept() throws InterruptedException {
        ApplicationContext finishing = ApplicationContext.builder().build();
        ApplicationContext starting = ApplicationContext.builder().build();
        Map<Object, Object> entries = new ConcurrentHashMap<>();
        entries.put(ApplicationContext.class, finishing);
        ExtensionContext context = proxy(ExtensionContext.class, (proxy, method, args) -> null);

        Thread[] publisher = new Thread[1];
        ExtensionContext.Store plainStore = store(entries, () -> { });
        TestExtension startingClass = new TestExtension(starting, plainStore);
        ExtensionContext.Store racingStore = store(entries, () -> {
            publisher[0] = new Thread(() -> startingClass.applicationContextRebuilt(context));
            publisher[0].start();
            awaitBlockedOrFinished(publisher[0]);
        });

        new TestExtension(finishing, racingStore).afterClass(context);
        publisher[0].join(10_000);

        assertSame(starting, entries.get(ApplicationContext.class), "the other class's context was removed");
    }

    private static void awaitBlockedOrFinished(Thread thread) {
        while (thread.getState() != Thread.State.BLOCKED && thread.getState() != Thread.State.TERMINATED) {
            Thread.onSpinWait();
        }
    }

    /**
     * @param afterGet Runs once, right after the first lookup has read its value.
     */
    private static ExtensionContext.Store store(Map<Object, Object> entries, Runnable afterGet) {
        boolean[] ran = new boolean[1];
        return proxy(ExtensionContext.Store.class, (proxy, method, args) -> switch (method.getName()) {
            case "get" -> {
                Object value = entries.get(args[0]);
                if (!ran[0]) {
                    ran[0] = true;
                    afterGet.run();
                }
                yield value;
            }
            case "put" -> entries.put(args[0], args[1]);
            case "remove" -> entries.remove(args[0]);
            default -> throw new UnsupportedOperationException(method.getName());
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static final class TestExtension extends MicronautJunit5Extension {
        private final ExtensionContext.Store store;

        private TestExtension(ApplicationContext applicationContext, ExtensionContext.Store store) {
            this.applicationContext = applicationContext;
            this.store = store;
        }

        @Override
        protected ExtensionContext.Store getStore(ExtensionContext context) {
            return store;
        }
    }
}
