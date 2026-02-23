/*
 * Copyright 2017-2025 original authors
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
package io.micronaut.test.leak;

import io.netty.util.LeakPresenceDetector;
import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.runtime.model.SpecInfo;

/**
 * Spock leak presence extension.
 */
public final class SpockLeakPresenceExtension implements IGlobalExtension {
    static {
        WithTransferableScope.init();
    }

    @Override
    public void visitSpec(SpecInfo spec) {
        spec.addSharedInitializerInterceptor(invocation -> {
            WithTransferableScope.SCOPE.set(new LeakPresenceDetector.ResourceScope(spec.getDisplayName()));
            invocation.proceed();
        });
        spec.addCleanupSpecInterceptor(invocation -> {
            invocation.proceed();
            LeakPresenceDetector.ResourceScope scope = WithTransferableScope.SCOPE.get();
            WithTransferableScope.SCOPE.remove();
            WithTransferableScope.closeWithGracePeriod(scope);
        });
    }
}
