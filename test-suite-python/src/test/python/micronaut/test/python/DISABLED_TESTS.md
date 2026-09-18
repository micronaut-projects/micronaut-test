# Python Docs Disabled Test Inventory

This file tracks the Python documentation tests of `test-suite-python` that are present but disabled, or that deviate
from the JUnit 5 example because the direct port currently fails with the Python compiler (`micronaut-inject-python`).
Use it as the bug-fixing task list.

## Reconciliation

- Last generated active `@Disabled` count: 2 classes (plus `DisableResolveParametersTest.bar` and
  `TestOutcomeListenerTest.test_disabled`, which are disabled on purpose like the Java examples).
- Last generated command: `rg -n "@Disabled\(" test-suite-python/src/test/python`.
- Last full-suite command: `./gradlew :test-suite-python:test -Ppython-ci`.
- Last full-suite result: build successful, 29 tests, 5 skipped (the two classes below, `RequiresTest`, and the two intentionally disabled methods).

## Migration Rules

- Python test classes are plain classes with `@Test` methods; `@MicronautTest` comes from
  `micronaut.test.extensions.junit5.annotation`. Every JUnit decorator from `org.junit.jupiter.api` (and its
  sub-packages) is copied to the compiled test class.
- `@ParameterizedTest` (`org.junit.jupiter.params`) is not available; loop over the test data inside a `@Test`.
- A single positional class-valued decorator argument that is not bound to a module-level name
  (`@TestMethodOrder(MethodOrderer.OrderAnnotation)`) is mistaken for bare decoration at runtime; pass it as a
  keyword argument (`@TestMethodOrder(value=MethodOrderer.OrderAnnotation)`).
- Test doubles are `@Singleton @Replaces(...)` classes guarded by `@Requires(property="spec.name", value=...)`,
  with `@Property(name="spec.name", ...)` on the test (see below for why `@MockBean` is not used).
- Methods overriding default methods of a Java interface (`TestExecutionListener`) need `@Executable`.

## Active `@Disabled` Tests

| Test | Reason |
| --- | --- |
| `micronaut.test.python.PropertySourceMapTest` | `TestPropertyProvider.getProperties()` is called by the extension before the application context (and with it the GraalPy runtime) exists: "GraalPy context has not been initialized". |
| `micronaut.test.python.MockBeanCollaboratorTest` | The AOP proxy generated for a `@MockBean` factory method (a `@Refreshable` `@Around` bean) whose type is a Python class calls the generated no-arg constructor of the stub, which creates an unrelated Python instance, and its `asPolyglotValue()` is not intercepted; every Python consumer of the proxy (the test, `MathController`) receives that fresh instance instead of the object returned by the factory method, so the test double is never used. A `@MockBean` method returning a Python *ABC* (compiled to a Java interface) fails earlier with `IncompatibleClassChangeError: ... $Intercepted has interface ... as super class`. |

## Intentionally Unsupported Snippet Targets

| Target | Reason |
| --- | --- |
| JUnit `@Nested` classes (`OrderServiceTest`, `OuterTest` of the JUnit 5 chapter) | A nested Python class is compiled to a separate top-level class (`OrderServiceTest$Placing`), so JUnit does not treat it as a nested test of the enclosing `@MicronautTest` class and it fails with "GraalPy context has not been initialized". |
| `MathMockServiceTest`/`MathCollaboratorTest` with Mockito | No Mockito for Python; hand-written test doubles registered with `@Replaces` are documented instead. |
