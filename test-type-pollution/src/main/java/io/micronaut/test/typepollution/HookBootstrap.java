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

import io.micronaut.core.annotation.Internal;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * Method handle bootstrap used by generated code to call into the {@link ConcreteCounter}. Must be
 * public for generated code, but should never be used directly.
 */
@SuppressWarnings("unused")
@Internal
public final class HookBootstrap {
    static final Method METHOD_STATIC_TYPE_CHECK;
    static final Method METHOD_DYNAMIC_TYPE_CHECK;
    static final Method METHOD_DYNAMIC_TYPE_CHECK_CAST;

    static {
        try {
            METHOD_STATIC_TYPE_CHECK = HookBootstrap.class.getDeclaredMethod("staticTypeCheck", MethodHandles.Lookup.class, String.class, MethodType.class, Class.class);
            METHOD_DYNAMIC_TYPE_CHECK = HookBootstrap.class.getDeclaredMethod("dynamicTypeCheck", MethodHandles.Lookup.class, String.class, MethodType.class);
            METHOD_DYNAMIC_TYPE_CHECK_CAST = HookBootstrap.class.getDeclaredMethod("dynamicTypeCheckCast", MethodHandles.Lookup.class, String.class, MethodType.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private HookBootstrap() {
    }

    /**
     * Called on successful type check in scenarios where the interface type is statically known,
     * i.e. checkcast and instanceof instructions. Returned method type is
     * {@code (Ljava/lang/Object;)V}, where the sole parameter is the object that was type checked.
     *
     * @param lookup        Required bootstrap method parameter
     * @param name          Required bootstrap method parameter
     * @param type          Required bootstrap method parameter
     * @param interfaceType The statically known interface type
     * @return {@code (Ljava/lang/Object;)V}
     */
    public static CallSite staticTypeCheck(MethodHandles.Lookup lookup, String name, MethodType type, Class<?> interfaceType) throws NoSuchMethodException, IllegalAccessException {
        if (!interfaceType.isInterface()) {
            // for non-interface types, do nothing
            return new ConstantCallSite(MethodHandles.empty(type));
        }

        return new ConstantCallSite(MethodHandles.collectArguments(
            new ClassSwitch(MethodType.methodType(void.class), 0) {
                @Override
                protected MethodHandle downstream(Class<?> concreteType) {
                    return ConcreteCounter.COUNTERS.get(concreteType).typeCheckHandle(interfaceType);
                }
            }.dynamicInvoker(),
            0,
            lookup.findVirtual(Object.class, "getClass", MethodType.methodType(Class.class))
        ));
    }

    /**
     * Called on successful type check in scenarios where the interface type is not statically
     * known, i.e. {@link Class#isAssignableFrom(Class)} and {@link Class#isInstance(Object)} calls.
     * Returned method type is {@code (Ljava/lang/Class;Ljava/lang/Class;)V}, where the first
     * parameter is the interface type and the second parameter is the concrete type.
     *
     * @param lookup        Required bootstrap method parameter
     * @param name          Required bootstrap method parameter
     * @param type          Required bootstrap method parameter
     * @return {@code (Ljava/lang/Class;Ljava/lang/Class;)V}
     */
    public static CallSite dynamicTypeCheck(MethodHandles.Lookup lookup, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
        return new ConstantCallSite(dynamicTypeCheckImpl(lookup));
    }

    /**
     * Called on successful type check for {@link Class#cast(Object)}. This is a slightly modified
     * {@link #dynamicTypeCheck} to make the cast implementation work without local variables.
     * Returned method type is {@code (Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;},
     * where the first parameter is the interface type and the second parameter is the concrete
     * value that was cast. The return value is the second parameter.
     *
     * @param lookup        Required bootstrap method parameter
     * @param name          Required bootstrap method parameter
     * @param type          Required bootstrap method parameter
     * @return {@code (Ljava/lang/Class;Ljava/lang/Object;)Ljava/lang/Object;}
     */
    public static CallSite dynamicTypeCheckCast(MethodHandles.Lookup lookup, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
        return new ConstantCallSite(MethodHandles.foldArguments(
            MethodHandles.dropArguments(MethodHandles.identity(Object.class), 0, Class.class),
            MethodHandles.collectArguments(
                dynamicTypeCheckImpl(lookup),
                1,
                lookup.findVirtual(Object.class, "getClass", MethodType.methodType(Class.class))
            )
        ));
    }

    private static MethodHandle dynamicTypeCheckImpl(MethodHandles.Lookup lookup) throws NoSuchMethodException, IllegalAccessException {
        MethodHandle switcher = new ClassSwitch(MethodType.methodType(void.class, Class.class), 0) {
            @Override
            protected MethodHandle downstream(Class<?> interfaceType) {
                return new ClassSwitch(MethodType.methodType(void.class), 0) {
                    @Override
                    protected MethodHandle downstream(Class<?> concreteType) {
                        return ConcreteCounter.COUNTERS.get(concreteType).typeCheckHandle(interfaceType);
                    }
                }.dynamicInvoker();
            }
        }.dynamicInvoker();
        return MethodHandles.guardWithTest(
            lookup.findVirtual(Class.class, "isInterface", MethodType.methodType(boolean.class)),
            switcher,
            MethodHandles.empty(MethodType.methodType(void.class, Class.class, Class.class))
        );
    }
}
