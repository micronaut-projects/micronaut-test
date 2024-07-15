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

import io.micronaut.core.annotation.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;

/**
 * This class recognizes the "focus changes" that are then forwarded to the {@link FocusListener}.
 * <p>
 * There is one {@link ConcreteCounter} for each concrete type. Each {@link ConcreteCounter} has an
 * {@link InterfaceHolder} for any interfaces that the concrete type is type checked against. Each
 * {@link InterfaceHolder} has a {@link MutableCallSite}. For the interface that was last type
 * checked, this call site does nothing. For any other interface, a type check leads to a "focus
 * event": The event is forwarded to the {@link FocusListener} for logging. The now-focused
 * interface changes its call site to do nothing, and the previously focused interface becomes
 * unfocused.
 */
final class ConcreteCounter {
    static final ClassValue<ConcreteCounter> COUNTERS = new ClassValue<>() {
        @Override
        protected ConcreteCounter computeValue(Class<?> type) {
            return new ConcreteCounter(type);
        }
    };

    @Nullable
    static FocusListener focusListener;

    private static final MethodHandle FOCUS;
    private static final MethodHandle IGNORE = MethodHandles.empty(MethodType.methodType(void.class));

    static {
        try {
            FOCUS = MethodHandles.lookup()
                .findVirtual(InterfaceHolder.class, "focus", MethodType.methodType(void.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private final Class<?> concreteType;

    private final ClassValue<InterfaceHolder> holdersByInterface = new ClassValue<>() {
        @Override
        protected InterfaceHolder computeValue(Class<?> type) {
            return new InterfaceHolder(type);
        }
    };
    private InterfaceHolder focused = null;

    private ConcreteCounter(Class<?> concreteType) {
        this.concreteType = concreteType;
    }

    MethodHandle typeCheckHandle(Class<?> interfaceType) {
        return holdersByInterface.get(interfaceType).callSite.dynamicInvoker();
    }

    final class InterfaceHolder {
        final Class<?> interfaceType;
        final MethodHandle focus = FOCUS.bindTo(this);
        final MutableCallSite callSite = new MutableCallSite(focus);

        InterfaceHolder(Class<?> interfaceType) {
            this.interfaceType = interfaceType;
        }

        private void unfocus() {
            assert Thread.holdsLock(ConcreteCounter.this);
            callSite.setTarget(focus);
        }

        private void focus() {
            FocusListener focusListener = ConcreteCounter.focusListener;
            if (focusListener != null) {
                focusListener.onFocus(concreteType, interfaceType);
            }
            synchronized (ConcreteCounter.this) {
                if (focused != null) {
                    focused.unfocus();
                }
                focused = this;
                callSite.setTarget(IGNORE);
            }
        }
    }
}
