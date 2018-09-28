package io.micronaut.test.spock.annotation;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;
import io.micronaut.inject.BeanDefinition;

import java.util.Optional;

public class SpecActiveCondition implements Condition {

    public static final String ACTIVE_SPEC_NAME = "micronaut.test.active.spec";

    @Override
    public boolean matches(ConditionContext context) {
        if (context.getComponent() instanceof BeanDefinition) {
            BeanDefinition<?> definition = (BeanDefinition<?>) context.getComponent();
            final BeanContext beanContext = context.getBeanContext();
            if (beanContext instanceof ApplicationContext) {
                final Optional<Class<?>> declaringType = definition.getDeclaringType();
                ApplicationContext applicationContext = (ApplicationContext) beanContext;
                if (definition.isAnnotationPresent(MockBean.class) && declaringType.isPresent()) {
                    final String activeSpecName = applicationContext.get(ACTIVE_SPEC_NAME, String.class).orElse(null);
                    return activeSpecName != null && activeSpecName.equals(declaringType.get().getName());

                } else {
                    final String activeSpecName = applicationContext.get(ACTIVE_SPEC_NAME, String.class).orElse(null);
                    return activeSpecName != null && activeSpecName.equals(definition.getBeanType().getName());
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
