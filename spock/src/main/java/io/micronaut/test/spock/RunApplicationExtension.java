package io.micronaut.test.spock;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.PropertySource;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.spock.annotation.MicronautTest;
import io.micronaut.test.spock.annotation.SpecActiveCondition;
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.SpecInfo;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class RunApplicationExtension extends AbstractAnnotationDrivenExtension<MicronautTest> {

    private ApplicationContext applicationContext;
    private EmbeddedApplication embeddedApplication;

    @Override
    public void visitSpecAnnotation(MicronautTest annotation, SpecInfo spec) {

        final ApplicationContextBuilder builder = ApplicationContext.build();
        final Property p = spec.getAnnotation(Property.class);

        Map<String, Object> props = new LinkedHashMap<>();
        if (p != null) {
            props.put(p.name(), p.value());
        }

        final PropertySource ps = spec.getAnnotation(PropertySource.class);
        if (ps != null) {
            for (Property property : ps.value()) {
                props.put(property.name(), property.value());
            }
        }
        props.put(SpecActiveCondition.ACTIVE_SPEC_NAME, spec.getPackage() + "." + spec.getName());
        builder.properties(props);
        this.applicationContext = builder.build();
        spec.addSetupSpecInterceptor(invocation -> {

            applicationContext.start();
            if (applicationContext.containsBean(EmbeddedApplication.class)) {
                embeddedApplication = applicationContext.getBean(EmbeddedApplication.class);
                embeddedApplication.start();
            }
            final Object sharedInstance = invocation.getSharedInstance();
            applicationContext.inject(sharedInstance);

            invocation.proceed();
        });

        spec.addCleanupSpecInterceptor(invocation -> {
            if (embeddedApplication != null) {
                embeddedApplication.stop();
            } else if(applicationContext != null) {
                applicationContext.stop();
            }
        });

        spec.addSetupInterceptor(invocation -> {
            final Object instance = invocation.getInstance();
            if (applicationContext != null) {
                applicationContext.inject(instance);
            }
        });
    }


}
