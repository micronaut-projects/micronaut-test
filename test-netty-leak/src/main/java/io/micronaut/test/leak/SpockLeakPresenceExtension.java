package io.micronaut.test.leak;

import io.netty.util.LeakPresenceDetector;
import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.runtime.model.SpecInfo;

public final class SpockLeakPresenceExtension implements IGlobalExtension {
    static {
        WithTransferableScope.init();
    }

    @Override
    public void visitSpec(SpecInfo spec) {
        spec.addSetupSpecInterceptor(invocation -> {
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
