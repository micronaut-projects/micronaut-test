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

/**
 * Public listener for responding to focus events.<p>
 * A focus event happens when a certain concrete type is successfully type checked against an
 * interface that it was not checked against immediately before:
 * <code><pre>
 * Impl impl = new Impl();
 * impl instanceof A // focus event
 * impl instanceof A // no focus event
 * impl instanceof A // no focus event
 * impl instanceof B // focus event
 * impl instanceof B // no focus event
 * impl instanceof A // focus event
 * </pre></code>
 * Each focus event may invalidate a cache field on the concrete class which can be especially
 * expensive on machines with many cores (JDK-8180450). Thus, such focus events should be kept off
 * the hot path when running on JDK versions that still have this bug.
 */
public interface FocusListener {
    /**
     * Set the global focus listener, or {@code null} to disable listening for focus events.
     *
     * @param focusListener The focus listener
     */
    static void setFocusListener(@Nullable FocusListener focusListener) {
        ConcreteCounter.focusListener = focusListener;
    }

    /**
     * Called on every focus event, potentially concurrently.
     *
     * @param concreteType  The concrete type that was checked
     * @param interfaceType The interface type that the concrete type was type checked against
     */
    void onFocus(Class<?> concreteType, Class<?> interfaceType);
}
