package io.micronaut.test.junit5.parallel;

import io.micronaut.test.extensions.junit5.MicronautTestResourceLocksProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs the fixtures in this package through a second, fully parallel JUnit Jupiter engine and
 * asserts the guarantees Micronaut Test makes about them.
 *
 * <p>The contract under test is: everything sharing one {@code @MicronautTest} application context
 * runs single-threaded, while separate {@code @MicronautTest} classes run at the same time.</p>
 *
 * @see io.micronaut.test.extensions.junit5.MicronautTestResourceLocksProvider
 */
@Isolated("starts its own parallel JUnit engine and toggles a global system property")
// Every test here drives the fixtures through a second JUnit Platform launcher, selecting them by
// class at run time. A native image resolves its tests at build time instead, from a fixed list, so
// a nested launcher discovers nothing and every run comes back empty.
@DisabledInNativeImage
class MicronautTestParallelExecutionTest {

    @Test
    @DisplayName("methods of one @MicronautTest class never overlap")
    void methodsOfOneClassAreSerialized() {
        ConcurrencyRecorder.reset(SharedContextFixture.KEY);

        assertAllPassed(runInParallel(SharedContextFixture.class), 6);

        assertEquals(6, ConcurrencyRecorder.invocations(SharedContextFixture.KEY));
        assertEquals(1, ConcurrencyRecorder.maxConcurrency(SharedContextFixture.KEY),
            "methods sharing one application context ran concurrently");
    }

    @Test
    @DisplayName("@Nested methods never overlap with the enclosing class")
    void nestedClassesAreSerializedWithTheirEnclosingClass() {
        ConcurrencyRecorder.reset(NestedClassFixture.KEY);

        assertAllPassed(runInParallel(NestedClassFixture.class), 5);

        assertEquals(5, ConcurrencyRecorder.invocations(NestedClassFixture.KEY));
        assertEquals(1, ConcurrencyRecorder.maxConcurrency(NestedClassFixture.KEY),
            "a @Nested class ran concurrently with the extension instance it shares");
    }

    @Test
    @DisplayName("parameterized, repeated and dynamic invocations never overlap")
    void testTemplateInvocationsAreSerialized() {
        ConcurrencyRecorder.reset(TestTemplateFixture.KEY);

        assertAllPassed(runInParallel(TestTemplateFixture.class), 12);

        assertEquals(12, ConcurrencyRecorder.invocations(TestTemplateFixture.KEY));
        assertEquals(1, ConcurrencyRecorder.maxConcurrency(TestTemplateFixture.KEY),
            "test template invocations sharing one application context ran concurrently");
    }

    @Test
    @DisplayName("separate @MicronautTest classes do run at the same time")
    void separateClassesRunInParallel() {
        Rendezvous.expect(SeparateContextsFixtureOne.KEY, 2);

        assertAllPassed(runInParallel(SeparateContextsFixtureOne.class, SeparateContextsFixtureTwo.class), 2);
    }

    @Test
    @DisplayName("separate embedded servers start and serve concurrently")
    void separateEmbeddedServersRunInParallel() {
        Rendezvous.expect(EmbeddedServerFixtureOne.KEY, 2);

        assertAllPassed(runInParallel(EmbeddedServerFixtureOne.class, EmbeddedServerFixtureTwo.class), 2);
    }

    @Test
    @DisplayName("a @MockBean is not shared by two running methods")
    void mockBeansAreNotSharedConcurrently() {
        ConcurrencyRecorder.reset("mock-bean");

        assertAllPassed(runInParallel(MockBeanFixture.class), 6);

        assertEquals(1, ConcurrencyRecorder.maxConcurrency("mock-bean"));
    }

    @Test
    @DisplayName("a method level @Property is not visible to another running method")
    void methodPropertiesDoNotLeakBetweenMethods() {
        ConcurrencyRecorder.reset("method-property");

        assertAllPassed(runInParallel(MethodPropertyFixture.class), 4);

        assertEquals(1, ConcurrencyRecorder.maxConcurrency("method-property"));
    }

    @Test
    @DisplayName("rebuildContext does not tear down a context a method is still using")
    void rebuiltContextsAreNotSharedConcurrently() {
        ConcurrencyRecorder.reset("rebuild-context");

        assertAllPassed(runInParallel(RebuildContextFixture.class), 4);

        assertEquals(1, ConcurrencyRecorder.maxConcurrency("rebuild-context"));
    }

    @Test
    @DisplayName("a PER_CLASS TestPropertyProvider survives parallel execution")
    void testPropertyProviderIsSerialized() {
        ConcurrencyRecorder.reset(TestPropertyProviderFixture.KEY);

        assertAllPassed(runInParallel(TestPropertyProviderFixture.class), 3);

        assertEquals(1, ConcurrencyRecorder.maxConcurrency(TestPropertyProviderFixture.KEY));
    }

    @Test
    @DisplayName("@Sql BEFORE_EACH and AFTER_EACH phases do not interleave")
    void sqlPhasesDoNotInterleave() {
        ConcurrencyRecorder.reset("sql-phase");

        assertAllPassed(runInParallel(SqlPhaseFixture.class), 4);

        assertEquals(1, ConcurrencyRecorder.maxConcurrency("sql-phase"));
    }

    @Test
    @DisplayName("parameter resolution stays correct under parallel execution")
    void parameterResolutionIsSerialized() {
        ConcurrencyRecorder.reset(ParameterResolutionFixture.KEY);

        assertAllPassed(runInParallel(ParameterResolutionFixture.class), 4);

        assertEquals(1, ConcurrencyRecorder.maxConcurrency(ParameterResolutionFixture.KEY));
    }

    @Test
    @DisplayName("TestExecutionListener callbacks never nest")
    void listenerCallbacksNeverNest() {
        ConcurrencyRecorder.reset(ListenerPairingFixture.KEY);
        PairingListener.reset();

        assertAllPassed(runInParallel(ListenerPairingFixture.class), 4);

        assertEquals(1, ConcurrencyRecorder.maxConcurrency(ListenerPairingFixture.KEY));
        assertEquals(1, PairingListener.MAX_DEPTH.get(),
            "beforeTestMethod/afterTestMethod overlapped, so listener state cannot be per-method");
        assertEquals(0, PairingListener.DEPTH.get(), "listener callbacks were not balanced");
    }

    @Test
    @DisplayName("a class declaring @Execution(CONCURRENT) opts out of the lock")
    void aClassCanOptOutOfSerialization() {
        Rendezvous.expect(ConcurrentMethodsFixture.KEY, 2);

        assertAllPassed(runInParallel(ConcurrentMethodsFixture.class), 2);
    }

    @Test
    @DisplayName("without the lock the very same fixture does overlap")
    void theSerializationAssertionsAreNotVacuous() {
        ConcurrencyRecorder.reset(SharedContextFixture.KEY);

        System.setProperty(MicronautTestResourceLocksProvider.PARALLEL_METHODS_PROPERTY, "true");
        try {
            runInParallel(SharedContextFixture.class);
        } finally {
            System.clearProperty(MicronautTestResourceLocksProvider.PARALLEL_METHODS_PROPERTY);
        }

        assertEquals(6, ConcurrencyRecorder.invocations(SharedContextFixture.KEY));
        assertTrue(ConcurrencyRecorder.maxConcurrency(SharedContextFixture.KEY) > 1,
            "the engine never ran two methods at once, so the serialization assertions prove nothing");
        assertTrue(ConcurrencyRecorder.distinctThreads(SharedContextFixture.KEY) > 1,
            "the engine never used a second thread, so the serialization assertions prove nothing");
    }

    private static EngineExecutionResults runInParallel(Class<?>... fixtures) {
        DiscoverySelector[] selectors = Arrays.stream(fixtures)
            .map(DiscoverySelectors::selectClass)
            .toArray(DiscoverySelector[]::new);
        return EngineTestKit.engine("junit-jupiter")
            .enableImplicitConfigurationParameters(false)
            .selectors(selectors)
            .configurationParameter("junit.jupiter.execution.parallel.enabled", "true")
            .configurationParameter("junit.jupiter.execution.parallel.mode.default", "concurrent")
            .configurationParameter("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")
            .configurationParameter("junit.jupiter.execution.parallel.config.strategy", "fixed")
            .configurationParameter("junit.jupiter.execution.parallel.config.fixed.parallelism", "4")
            .execute();
    }

    private static void assertAllPassed(EngineExecutionResults results, int expected) {
        results.testEvents().assertStatistics(stats -> stats.started(expected).succeeded(expected).failed(0));
        results.containerEvents().assertStatistics(stats -> stats.failed(0));
    }
}
