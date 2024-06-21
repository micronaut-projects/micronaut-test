package io.micronaut.test.typepollution;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantValue", "UnusedReturnValue", "SameParameterValue"})
class VisitorWrapperImplTest {
    @BeforeAll
    static void setup() {
        ByteBuddyAgent.install();

        hook(Reset.class);
    }

    static void hook(Class<?> cl) {
        try (DynamicType.Unloaded<?> unloaded = new ByteBuddy()
            .redefine(cl)
            .visit(new VisitorWrapperImpl())
            .make()) {

            unloaded.load(cl.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
        }
    }

    @BeforeEach
    void reset() {
        new Reset().reset(new Impl());
    }

    @Test
    public void checkCast() {
        hook(CheckCast.class);

        TrackingFocusListener listener = new TrackingFocusListener().install();

        CheckCast cl = new CheckCast();

        cl.a(new Impl());
        cl.a(new Impl());

        cl.b(new Impl());
        cl.b(new Impl());
        cl.b(new Impl());

        cl.a(new Impl());

        cl.a(null);
        try {
            cl.a(1);
            Assertions.fail();
        } catch (ClassCastException ignored) {
            // expected
        }

        cl.concrete(new Impl());

        List<Entry> entries = listener.uninstall();
        Assertions.assertEquals(
            List.of(
                new Entry(Impl.class, A.class),
                new Entry(Impl.class, B.class),
                new Entry(Impl.class, A.class)
            ),
            entries
        );
    }

    @Test
    public void instanceOf() {
        hook(InstanceOf.class);

        TrackingFocusListener listener = new TrackingFocusListener().install();

        InstanceOf cl = new InstanceOf();

        Assertions.assertTrue(cl.a(new Impl()));
        Assertions.assertTrue(cl.a(new Impl()));

        Assertions.assertTrue(cl.b(new Impl()));
        Assertions.assertTrue(cl.b(new Impl()));
        Assertions.assertTrue(cl.b(new Impl()));

        Assertions.assertTrue(cl.a(new Impl()));

        Assertions.assertFalse(cl.a(null));
        Assertions.assertFalse(cl.a(1));

        Assertions.assertTrue(cl.concrete(new Impl()));

        List<Entry> entries = listener.uninstall();
        Assertions.assertEquals(
            List.of(
                new Entry(Impl.class, A.class),
                new Entry(Impl.class, B.class),
                new Entry(Impl.class, A.class)
            ),
            entries
        );
    }

    @Test
    public void reflIsInstance() {
        hook(ReflIsInstance.class);

        TrackingFocusListener listener = new TrackingFocusListener().install();

        ReflIsInstance cl = new ReflIsInstance();

        Assertions.assertTrue(cl.a(new Impl()));
        Assertions.assertTrue(cl.a(new Impl()));

        Assertions.assertTrue(cl.b(new Impl()));
        Assertions.assertTrue(cl.b(new Impl()));
        Assertions.assertTrue(cl.b(new Impl()));

        Assertions.assertTrue(cl.a(new Impl()));

        Assertions.assertFalse(cl.a(null));
        Assertions.assertFalse(cl.a(1));

        Assertions.assertTrue(cl.concrete(new Impl()));

        List<Entry> entries = listener.uninstall();
        Assertions.assertEquals(
            List.of(
                new Entry(Impl.class, A.class),
                new Entry(Impl.class, B.class),
                new Entry(Impl.class, A.class)
            ),
            entries
        );
    }

    @Test
    public void reflCast() {
        hook(ReflCast.class);

        TrackingFocusListener listener = new TrackingFocusListener().install();

        ReflCast cl = new ReflCast();

        cl.a(new Impl());
        cl.a(new Impl());

        cl.b(new Impl());
        cl.b(new Impl());
        cl.b(new Impl());

        cl.a(new Impl());

        cl.a(null);
        try {
            cl.a(1);
            Assertions.fail();
        } catch (ClassCastException ignored) {
            // expected
        }

        cl.concrete(new Impl());

        List<Entry> entries = listener.uninstall();
        Assertions.assertEquals(
            List.of(
                new Entry(Impl.class, A.class),
                new Entry(Impl.class, B.class),
                new Entry(Impl.class, A.class)
            ),
            entries
        );
    }

    @Test
    public void reflIsAssignableFrom() {
        hook(ReflIsAssignableFrom.class);

        TrackingFocusListener listener = new TrackingFocusListener().install();

        ReflIsAssignableFrom cl = new ReflIsAssignableFrom();

        Assertions.assertTrue(cl.a(Impl.class));
        Assertions.assertTrue(cl.a(Impl.class));

        Assertions.assertTrue(cl.b(Impl.class));
        Assertions.assertTrue(cl.b(Impl.class));
        Assertions.assertTrue(cl.b(Impl.class));

        Assertions.assertTrue(cl.a(Impl.class));

        Assertions.assertFalse(cl.a(Object.class));
        Assertions.assertFalse(cl.a(int.class));

        Assertions.assertTrue(cl.concrete(Impl.class));

        List<Entry> entries = listener.uninstall();
        Assertions.assertEquals(
            List.of(
                new Entry(Impl.class, A.class),
                new Entry(Impl.class, B.class),
                new Entry(Impl.class, A.class)
            ),
            entries
        );
    }

    private interface A {
    }

    private interface B {
    }

    private interface ResetItf {
    }

    private static final class Impl implements A, B, ResetItf {
    }

    static final class TrackingFocusListener implements FocusListener {
        private final List<Entry> entries = new ArrayList<>();

        @Override
        public synchronized void onFocus(Class<?> concreteType, Class<?> interfaceType) {
            entries.add(new Entry(concreteType, interfaceType));
        }

        TrackingFocusListener install() {
            FocusListener.setFocusListener(this);
            return this;
        }

        List<Entry> uninstall() {
            FocusListener.setFocusListener(null);
            return entries;
        }
    }

    record Entry(Class<?> concreteType, Class<?> interfaceType) {
        @Override
        public String toString() {
            // simple name is a bit nicer
            return "Entry[concreteType=" + concreteType.getSimpleName() + ", interfaceType=" + interfaceType.getSimpleName() + "]";
        }
    }

    static final class Reset {
        boolean reset(Object o) {
            return o instanceof ResetItf;
        }
    }

    static final class CheckCast {
        A a(Object o) {
            return (A) o;
        }

        B b(Object o) {
            return (B) o;
        }

        Impl concrete(Object o) {
            return (Impl) o;
        }
    }

    static final class InstanceOf {
        boolean a(Object o) {
            return o instanceof A;
        }

        boolean b(Object o) {
            return o instanceof B;
        }

        boolean concrete(Object o) {
            return o instanceof Impl;
        }
    }

    static final class ReflIsInstance {
        private final Class<?> a = A.class;
        private final Class<?> b = B.class;
        private final Class<?> impl = Impl.class;

        boolean a(Object o) {
            return a.isInstance(o);
        }

        boolean b(Object o) {
            return b.isInstance(o);
        }

        boolean concrete(Object o) {
            return impl.isInstance(o);
        }
    }

    static final class ReflCast {
        private final Class<A> a = A.class;
        private final Class<B> b = B.class;
        private final Class<Impl> impl = Impl.class;

        Object a(Object o) {
            return a.cast(o);
        }

        Object b(Object o) {
            return b.cast(o);
        }

        Object concrete(Object o) {
            return impl.cast(o);
        }
    }

    static final class ReflIsAssignableFrom {
        private final Class<?> a = A.class;
        private final Class<?> b = B.class;
        private final Class<?> impl = Impl.class;

        boolean a(Class<?> cl) {
            return a.isAssignableFrom(cl);
        }

        boolean b(Class<?> cl) {
            return b.isAssignableFrom(cl);
        }

        boolean concrete(Class<?> cl) {
            return impl.isAssignableFrom(cl);
        }
    }
}
