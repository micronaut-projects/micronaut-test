package io.micronaut.test.typepollution;

import net.bytebuddy.agent.ByteBuddyAgent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ThresholdFocusListenerTest {
    @BeforeAll
    static void setup() {
        ByteBuddyAgent.install();
        VisitorWrapperImplTest.hook(FlipFlop.class);
    }

    @Test
    void belowThreshold() {
        ThresholdFocusListener listener = new ThresholdFocusListener();
        FocusListener.setFocusListener(listener);

        FlipFlop flipFlop = new FlipFlop();
        for (int i = 0; i < 200; i++) {
            flipFlop.flipFlop(new Impl());
        }

        Assertions.assertTrue(listener.checkThresholds(1000));
    }

    @Test
    void aboveThreshold() {
        ThresholdFocusListener listener = new ThresholdFocusListener();
        FocusListener.setFocusListener(listener);

        FlipFlop flipFlop = new FlipFlop();
        for (int i = 0; i < 1000; i++) {
            flipFlop.flipFlop(new Impl());
        }

        Assertions.assertFalse(listener.checkThresholds(1000));
    }

    private interface A {
    }

    private interface B {
    }

    private static final class Impl implements A, B {
    }

    private static class FlipFlop {
        boolean flipFlop(Object o) {
            return (o instanceof A) ^ (o instanceof B);
        }
    }
}
