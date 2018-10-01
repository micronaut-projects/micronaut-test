package io.micronaut.test.spock;

import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.test.spock.annotation.MockBean;
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.model.FieldInfo;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

public class MockBeanExtension extends AbstractAnnotationDrivenExtension<MockBean> {

    @Override
    public void visitFieldAnnotation(MockBean annotation, FieldInfo field) {
        List<IMethodInterceptor> setupSpecInterceptors = field.getParent().getTopSpec().getSetupSpecInterceptors();
        Optional<IMethodInterceptor> first = setupSpecInterceptors.stream().filter(pd -> pd instanceof ApplicationContextProvider).findFirst();

        if (first.isPresent()) {
            ApplicationContextProvider provider = (ApplicationContextProvider) first.get();

            field.getParent().getTopSpec().addSetupSpecInterceptor(invocation -> {
                Object target = invocation.getInstance();

                Object v = field.readValue(target);
                provider.getApplicationContext().registerSingleton(v);
            });
        }
    }
}
