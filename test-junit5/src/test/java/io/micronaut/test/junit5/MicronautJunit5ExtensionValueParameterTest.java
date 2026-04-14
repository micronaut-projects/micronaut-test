package io.micronaut.test.junit5;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Executable;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.AnnotationMetadata;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

            Argument<String> argument = expressionArgument(true);

            assertNull(extension.invokeResolveValueParameter(
                parameterContextFor(NonBeanExecutable.class.getDeclaredMethod("placeholderMethod", String.class)),
                argument,
                "#{ null }"
            ));
        }
    }

    @Test
    void resolveValueParameterRejectsNullExpressionsForNonNullableArguments() throws Throwable {
        try (ApplicationContext applicationContext = ApplicationContext.run()) {
            TestMicronautJunit5Extension extension = new TestMicronautJunit5Extension();
            extension.setApplicationContext(applicationContext);
            Argument<String> argument = expressionArgument(false);

            ParameterResolutionException exception = assertThrows(
                ParameterResolutionException.class,
                () -> extension.invokeResolveValueParameter(
                    parameterContextFor(NonBeanExecutable.class.getDeclaredMethod("placeholderMethod", String.class)),
                    argument,
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
        ParameterContext parameterContext = mock(ParameterContext.class);
        when(parameterContext.getDeclaringExecutable()).thenReturn(method);
        when(parameterContext.getIndex()).thenReturn(0);
        when(parameterContext.getParameter()).thenReturn(method.getParameters()[0]);
        return parameterContext;
    }

    @SuppressWarnings("unchecked")
    private static Argument<String> expressionArgument(boolean nullable) {
        Argument<String> argument = mock(Argument.class);
        AnnotationMetadata annotationMetadata = mock(AnnotationMetadata.class);
        when(argument.getAnnotationMetadata()).thenReturn(annotationMetadata);
        when(argument.isDeclaredNullable()).thenReturn(nullable);
        when(annotationMetadata.hasEvaluatedExpressions()).thenReturn(true);
        when(annotationMetadata.getValue(eq(Value.class), eq(argument))).thenReturn(Optional.empty());
        return argument;
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
    }

    static final class NonBeanExecutable {
        void placeholderMethod(String value) {
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
