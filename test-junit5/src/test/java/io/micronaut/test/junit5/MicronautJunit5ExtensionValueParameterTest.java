package io.micronaut.test.junit5;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Executable;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MicronautJunit5ExtensionValueParameterTest {

    @Test
    void resolveValueParameterUsesBeanResolutionContextForBeanDefinitions() throws Throwable {
        try (ApplicationContext applicationContext = ApplicationContext.run(Map.of("foo.bar", "test"))) {
            TestMicronautJunit5Extension extension = new TestMicronautJunit5Extension();
            extension.setApplicationContext(applicationContext);

            assertEquals("test", extension.resolveParameter(parameterContextFor(TestBean.class.getDeclaredMethod("placeholderMethod", String.class)), null));
            assertEquals(2, extension.resolveParameter(parameterContextFor(TestBean.class.getDeclaredMethod("expressionMethod", Integer.class)), null));
        }
    }

    @Test
    void resolveValueParameterWrapsBeanResolutionFailures() throws Exception {
        try (ApplicationContext applicationContext = ApplicationContext.run()) {
            TestMicronautJunit5Extension extension = new TestMicronautJunit5Extension();
            extension.setApplicationContext(applicationContext);

            ParameterResolutionException exception = assertThrows(
                ParameterResolutionException.class,
                () -> extension.resolveParameter(parameterContextFor(TestBean.class.getDeclaredMethod("missingPropertyMethod", String.class)), null)
            );

            assertEquals("Unresolvable property specified to @Value: ${missing.property}", exception.getMessage());
            assertNotNull(exception.getCause());
        }
    }

    @Test
    void resolveValueParameterReturnsNullForNullableExpressions() throws Throwable {
        try (ApplicationContext applicationContext = ApplicationContext.run()) {
            TestMicronautJunit5Extension extension = new TestMicronautJunit5Extension();
            extension.setApplicationContext(applicationContext);

            assertNull(extension.invokeResolveValueParameter(
                parameterContextFor(NonBeanExecutable.class.getDeclaredMethod("placeholderMethod", String.class)),
                expressionArgument(applicationContext, "nullableExpressionMethod"),
                "#{ null }"
            ));
        }
    }

    @Test
    void resolveValueParameterRejectsNullExpressionsForNonNullableArguments() throws Throwable {
        try (ApplicationContext applicationContext = ApplicationContext.run()) {
            TestMicronautJunit5Extension extension = new TestMicronautJunit5Extension();
            extension.setApplicationContext(applicationContext);

            ParameterResolutionException exception = assertThrows(
                ParameterResolutionException.class,
                () -> extension.invokeResolveValueParameter(
                    parameterContextFor(NonBeanExecutable.class.getDeclaredMethod("placeholderMethod", String.class)),
                    expressionArgument(applicationContext, "nonNullableExpressionMethod"),
                    "#{ null }"
                )
            );

            assertEquals("Unresolvable property specified to @Value: #{ null }", exception.getMessage());
        }
    }

    @Test
    void resolveValueParameterFallsBackToPlaceholderResolutionWhenBeanDefinitionIsUnavailable() throws Throwable {
        try (ApplicationContext applicationContext = ApplicationContext.run(Map.of("foo.bar", "test"))) {
            TestMicronautJunit5Extension extension = new TestMicronautJunit5Extension();
            extension.setApplicationContext(applicationContext);

            assertEquals(
                "test",
                extension.invokeResolveValueParameter(
                    parameterContextFor(NonBeanExecutable.class.getDeclaredMethod("placeholderMethod", String.class)),
                    Argument.of(String.class),
                    "${foo.bar}"
                )
            );
        }
    }

    @Test
    void resolveValueParameterFailsWhenFallbackCannotResolvePlaceholder() throws Exception {
        try (ApplicationContext applicationContext = ApplicationContext.run()) {
            TestMicronautJunit5Extension extension = new TestMicronautJunit5Extension();
            extension.setApplicationContext(applicationContext);

            ParameterResolutionException exception = assertThrows(
                ParameterResolutionException.class,
                () -> extension.invokeResolveValueParameter(
                    parameterContextFor(NonBeanExecutable.class.getDeclaredMethod("placeholderMethod", String.class)),
                    Argument.of(String.class),
                    "${missing.property}"
                )
            );

            assertEquals("Unresolvable property specified to @Value: ${missing.property}", exception.getMessage());
        }
    }

    private static ParameterContext parameterContextFor(Method method) {
        return new TestParameterContext(method, 0);
    }

    private static Argument<?> expressionArgument(ApplicationContext applicationContext, String methodName) throws NoSuchMethodException {
        return applicationContext.getExecutableMethod(TestBean.class, methodName, String.class).getArguments()[0];
    }

    @Singleton
    static final class TestBean {
        @Executable
        void placeholderMethod(@Value("${foo.bar}") String value) {
        }

        @Executable
        void expressionMethod(@Value("#{1 + 1}") Integer value) {
        }

        @Executable
        void missingPropertyMethod(@Value("${missing.property}") String value) {
        }

        @Executable
        void nullableExpressionMethod(@Nullable @Value("#{ null }") String value) {
        }

        @Executable
        void nonNullableExpressionMethod(@Value("#{ null }") String value) {
        }
    }

    static final class NonBeanExecutable {
        void placeholderMethod(String value) {
        }
    }

    private record TestParameterContext(java.lang.reflect.Executable declaringExecutable, int index) implements ParameterContext {
        @Override
        public java.lang.reflect.Parameter getParameter() {
            return declaringExecutable.getParameters()[index];
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public Optional<Object> getTarget() {
            return Optional.empty();
        }
    }

    private static final class TestMicronautJunit5Extension extends MicronautJunit5Extension {
        private void setApplicationContext(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        private Object invokeResolveValueParameter(ParameterContext parameterContext, Argument<?> argument, String value) throws Throwable {
            Method method = MicronautJunit5Extension.class.getDeclaredMethod("resolveValueParameter", ParameterContext.class, Argument.class, String.class);
            method.setAccessible(true);
            try {
                return method.invoke(this, parameterContext, argument, value);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }
}
