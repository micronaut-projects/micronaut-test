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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.util.ArrayList;
import java.util.List;

/**
 * This class dynamically builds and maintains a {@link MethodHandle} that calls any of a number of
 * downstream {@link MethodHandle}s, depending on a {@link Class} parameter. If a new {@link Class}
 * is passed to this {@link MethodHandle}, it is mutated to gain a branch for that {@link Class} to
 * enable fast dispatch in the future.
 */
abstract class ClassSwitch {
    private static final MethodHandle FALLBACK;
    private static final MethodHandle CLASS_EQUALS;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            FALLBACK = lookup.findVirtual(ClassSwitch.class, "fallback", MethodType.methodType(void.class, Class.class));
            CLASS_EQUALS = MethodHandles.explicitCastArguments(
                lookup.findVirtual(Object.class, "equals", MethodType.methodType(boolean.class, Object.class)),
                MethodType.methodType(boolean.class, Class.class, Class.class)
            );
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private final MutableCallSite callSite;
    private final int switchPosition;

    /**
     * @param expectedType The type of the downstream method returned by {@link #downstream(Class)}
     * @param switchPosition The index in the parameter list where the {@link Class} parameter
     *                       should be added that this {@link ClassSwitch} will switch on
     */
    ClassSwitch(MethodType expectedType, int switchPosition) {
        List<Class<?>> combinedArgs = new ArrayList<>(expectedType.parameterList());
        combinedArgs.add(switchPosition, Class.class);
        this.callSite = new MutableCallSite(
            MethodHandles.dropArguments(
                MethodHandles.throwException(expectedType.returnType(), UnsupportedOperationException.class).bindTo(new UnsupportedOperationException("Type not bound yet")),
                0,
                combinedArgs
            )
        );
        this.switchPosition = switchPosition;

        // replace the call site with a handle that first calls fallback and then calls the call
        // site again. The fallback call will update the call site to include the new type.
        callSite.setTarget(MethodHandles.foldArguments(
            dynamicInvoker(),
            switchPosition,
            FALLBACK.bindTo(this)
        ));
    }

    /**
     * Build a {@link MethodHandle} that invokes this switch statement.
     */
    public final MethodHandle dynamicInvoker() {
        return callSite.dynamicInvoker();
    }

    private synchronized void fallback(Class<?> cl) {
        MethodHandle test = CLASS_EQUALS.bindTo(cl);
        if (switchPosition != 0) {
            test = MethodHandles.dropArguments(test, 0, callSite.getTarget().type().parameterList().subList(0, switchPosition));
        }
        callSite.setTarget(MethodHandles.guardWithTest(
            test,
            MethodHandles.dropArguments(downstream(cl), switchPosition, List.of(Class.class)),
            callSite.getTarget()
        ));
    }

    /**
     * Construct a new downstream {@link MethodHandle} for the given type.
     *
     * @param cl The type that was passed to this switch statement
     * @return The method handle that will be called every time this type is seen
     */
    protected abstract MethodHandle downstream(Class<?> cl);
}
