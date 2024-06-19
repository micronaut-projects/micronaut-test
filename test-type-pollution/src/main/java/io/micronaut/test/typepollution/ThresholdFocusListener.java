/*
 * Copyright 2017-2024 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.test.typepollution;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * {@link FocusListener} implementation that counts and tracks all focus events, including stack
 * traces. At the end of the test, {@link #checkThresholds(long)} can be used to verify that no
 * concrete class exceeded a given focus event threshold.
 */
public final class ThresholdFocusListener implements FocusListener {
    private static final StackWalker WALKER = StackWalker.getInstance(Set.of(StackWalker.Option.SHOW_REFLECT_FRAMES, StackWalker.Option.RETAIN_CLASS_REFERENCE));

    private final Map<Class<?>, ConcreteTracker> trackers = new ConcurrentHashMap<>();

    @Override
    public void onFocus(Class<?> concreteType, Class<?> interfaceType) {
        trackers.computeIfAbsent(concreteType, ConcreteTracker::new).onFocus(interfaceType);
    }

    /**
     * Check whether the number of focus events exceeded the given threshold for any concrete
     * class. If this is the case, this method also prints a human-readable report on all type
     * checking stack traces for the concrete type to {@code System.out}.
     *
     * @param threshold The number of events that should not be exceeded
     * @return {@code false} iff any concrete class was type checked too often
     */
    public boolean checkThresholds(long threshold) {
        boolean failed = false;
        for (ConcreteTracker concrete : trackers.values()) {
            long sum = concrete.total.sum();
            if (sum >= threshold) {
                failed = true;
                System.out.println("Concrete type: " + concrete.concreteType.getName() + " (" + sum + " hits)");
                for (InterfaceTracker itf : concrete.interfaces.values()) {
                    System.out.println("  Interface type: " + itf.interfaceType.getName());
                    itf.counts.entrySet().stream()
                        .sorted(Comparator.<Map.Entry<?, Long>>comparingLong(Map.Entry::getValue).reversed())
                        .forEach(e -> {
                            System.out.println("    Stack: (" + e.getValue() + " hits)");
                            for (StackTraceElement element : itf.stacks.get(e.getKey())) {
                                System.out.println("      " + element.toString());
                            }
                        });
                }
            }
        }
        return !failed;
    }

    private static final class ConcreteTracker {
        private final Class<?> concreteType;
        private final Map<Class<?>, InterfaceTracker> interfaces = new ConcurrentHashMap<>();
        private final LongAdder total = new LongAdder();

        ConcreteTracker(Class<?> concreteType) {
            this.concreteType = concreteType;
        }

        void onFocus(Class<?> interfaceType) {
            total.increment();
            interfaces.computeIfAbsent(interfaceType, InterfaceTracker::new).onFocus();
        }
    }

    private static final class InterfaceTracker {
        private final Class<?> interfaceType;
        private final Map<Long, Long> counts = new ConcurrentHashMap<>();
        private final Map<Long, StackTraceElement[]> stacks = new ConcurrentHashMap<>();

        InterfaceTracker(Class<?> interfaceType) {
            this.interfaceType = interfaceType;
        }

        void onFocus() {
            Long hash = WALKER.walk(s -> s.mapToLong(sf -> sf.getDeclaringClass().hashCode() * 31L * 31 + sf.getMethodName().hashCode() * 31L + sf.getMethodType().hashCode())
                .reduce(0, (a, b) -> a * 31 + b));
            if (counts.compute(hash, (k, oldV) -> oldV == null ? 1L : Math.incrementExact(oldV)) == 1L) {
                stacks.put(hash, WALKER.walk(s ->
                    // InterfaceHolder.focus is the first stack frame in the focus chain
                    s.dropWhile(f -> f.getDeclaringClass() != ConcreteCounter.InterfaceHolder.class)
                        .skip(1) // skip the InterfaceHolder itself
                        .map(StackWalker.StackFrame::toStackTraceElement)
                        .toArray(StackTraceElement[]::new)));
            }
        }
    }
}
