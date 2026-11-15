# Python Docs Disabled Test Inventory

This file tracks the Python documentation tests of `test-suite-python` that are present but disabled, or that deviate
from the JUnit 5 example because the direct port currently fails with the Python compiler (`micronaut-inject-python`).
Use it as the bug-fixing task list.

## Reconciliation

- Last generated active `@Disabled` count: 0 classes (plus `DisableResolveParametersTest.bar` and
  `TestOutcomeListenerTest.test_disabled`, which are disabled on purpose like the Java examples).
- Last generated command: `rg -n "@Disabled\(" test-suite-python/src/test/python`.
- Last full-suite command: `./gradlew :test-suite-python:test -Ppython-ci --max-workers=1`.
- Last full-suite result (micronaut-core 5.2.3, micronaut-build 8.1.2): build successful, 32 tests, 4 skipped (the class below, `RequiresTest`, and the two intentionally disabled methods).

## Migration Rules

- Python test classes are plain classes with `@Test` methods; `@MicronautTest` comes from
  `micronaut.test.extensions.junit5.annotation`. Every JUnit decorator from `org.junit.jupiter.api` (and its
  sub-packages) is copied to the compiled test class.
- `@ParameterizedTest` (`org.junit.jupiter.params`) is not available; loop over the test data inside a `@Test`.
- Test doubles are either `@Singleton @Replaces(...)` classes guarded by `@Requires(property="spec.name", value=...)`,
  with `@Property(name="spec.name", ...)` on the test (`MathMockServiceTest`, `MathCollaboratorTest`), or returned by a
  `@MockBean` method of the test (`MockBeanCollaboratorTest`); Python code receives the refreshable proxy as a Python
  proxy of the test double, so its attributes are read and written through it.
- JUnit `@Nested` classes are supported (`OrderServiceTest`): a nested Python class compiles to an inner class of the
  generated test class. The enclosing class needs a `@Test` method of its own: a `@MicronautTest` class whose tests all
  live in nested classes is generated with the constructors of an ordinary class and JUnit rejects it
  ("must declare a single constructor").
- A test can implement `TestPropertyProvider` (`PropertySourceMapTest`) with the `PER_CLASS` lifecycle, as in Java. Its
  Python object is created before the application context exists, in a GraalPy context that the application context
  claims as its primary context once it starts (requires the micronaut-core fix for this). That context is built from
  the default GraalPy configuration, so the `graalpy.context.*` and `graalpy.engine.*` settings of the application do
  not apply to it.

## Active `@Disabled` Tests

| Test | Reason |
| --- | --- |
| None | |

## Intentionally Unsupported Snippet Targets

| Target | Reason |
| --- | --- |
| `MathMockServiceTest`/`MathCollaboratorTest` with Mockito | No Mockito for Python; hand-written test doubles registered with `@Replaces` are documented instead. |
